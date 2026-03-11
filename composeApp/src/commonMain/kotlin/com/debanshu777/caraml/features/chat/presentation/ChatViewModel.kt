package com.debanshu777.caraml.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu777.caraml.core.domain.InferenceRepository
import com.debanshu777.caraml.core.domain.ModelLoadResult
import com.debanshu777.caraml.core.storage.localmodel.LocalModelEntity
import com.debanshu777.caraml.core.storage.localmodel.LocalModelRepository
import com.debanshu777.caraml.features.chat.data.ChatMessage
import com.debanshu777.caraml.features.chat.data.LiveGenerationStats
import com.debanshu777.caraml.features.chat.data.MessageRole
import com.debanshu777.caraml.features.chat.data.TokenTimer
import com.debanshu777.runner.StopReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ChatViewModel(
    private val localModelRepository: LocalModelRepository,
    private val inferenceRepository: InferenceRepository,
) : ViewModel() {

    private val allDownloadedModels: StateFlow<List<LocalModelEntity>> =
        localModelRepository.getAllDownloadedFiles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val topModels: StateFlow<List<LocalModelEntity>> =
        allDownloadedModels
            .map { models -> models.take(3) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedModel = MutableStateFlow<LocalModelEntity?>(null)
    val selectedModel: StateFlow<LocalModelEntity?> = _selectedModel.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.NoModels)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var modelLoadJob: Job? = null
    private var generationJob: Job? = null

    init {
        viewModelScope.launch {
            allDownloadedModels.collect { models ->
                if (_selectedModel.value == null && models.isNotEmpty()) {
                    _selectedModel.value = models.first()
                } else if (models.isEmpty()) {
                    _uiState.value = ChatUiState.NoModels
                }
            }
        }

        viewModelScope.launch {
            _selectedModel.filterNotNull().collect { model ->
                loadModel(model)
            }
        }
    }

    fun selectModel(model: LocalModelEntity) {
        _selectedModel.value = model
        viewModelScope.launch {
            localModelRepository.incrementUsageCount(model.modelId, model.filename)
        }
    }

    private fun loadModel(model: LocalModelEntity) {
        modelLoadJob?.cancel()
        generationJob?.cancel()
        
        _uiState.value = ChatUiState.ModelLoading

        modelLoadJob = viewModelScope.launch(Dispatchers.Default) {
            when (val result = inferenceRepository.loadModel(model)) {
                is ModelLoadResult.Success -> {
                    _uiState.value = ChatUiState.Ready()
                }
                is ModelLoadResult.Error -> {
                    _uiState.value = ChatUiState.ModelError(result.message)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val currentState = _uiState.value
        if (currentState !is ChatUiState.Ready || currentState.isGenerating) return

        generationJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                // Phase 1: Proactive context check
                if (inferenceRepository.isContextAboveThreshold()) {
                    performContextReset(currentState.messages)
                }

                // Add user message and placeholder assistant message
                val userMessage = ChatMessage(role = MessageRole.User, text = text.trim())
                val assistantMessage = ChatMessage(role = MessageRole.Assistant, text = "")

                _uiState.update { state ->
                    if (state is ChatUiState.Ready) {
                        state.copy(
                            messages = state.messages + userMessage + assistantMessage,
                            isGenerating = true
                        )
                    } else state
                }

                // Generate response
                var wasCancelled = false
                val timer = TokenTimer()

                try {
                    inferenceRepository.generateResponse(userMessage.text)
                        .onEach { timer.onToken() }
                        .conflate()
                        .flowOn(Dispatchers.IO)
                        .collect { token ->
                            _uiState.update { state ->
                                if (state is ChatUiState.Ready) {
                                    val messages = state.messages.toMutableList()
                                    val idx = messages.indexOfFirst { it.id == assistantMessage.id }
                                    if (idx >= 0) {
                                        messages[idx] = messages[idx].copy(text = messages[idx].text + token)
                                    }
                                    
                                    val (tokenCount, tokensPerSecond) = timer.buildLiveMetrics()
                                    val liveStats = LiveGenerationStats(
                                        contextUsed = inferenceRepository.getContextUsed(),
                                        contextLimit = inferenceRepository.getContextLimit(),
                                        outputTokenCount = tokenCount,
                                        tokensPerSecond = tokensPerSecond
                                    )
                                    
                                    state.copy(
                                        messages = messages,
                                        isGenerating = true,
                                        liveStats = liveStats
                                    )
                                } else state
                            }
                        }
                } catch (e: CancellationException) {
                    wasCancelled = true
                    throw e
                } catch (_: Exception) {
                    _uiState.update { state ->
                        if (state is ChatUiState.Ready) {
                            val messages = state.messages.toMutableList()
                            val idx = messages.indexOfFirst { it.id == assistantMessage.id }
                            if (idx >= 0) {
                                messages[idx] = messages[idx].copy(
                                    text = "Something went wrong. Please try again."
                                )
                            }
                            state.copy(messages = messages, isGenerating = false)
                        } else state
                    }
                } finally {
                    if (!wasCancelled) {
                        _uiState.update { state ->
                            if (state is ChatUiState.Ready) {
                                val messages = state.messages.toMutableList()
                                val idx = messages.indexOfFirst { it.id == assistantMessage.id }
                                if (idx >= 0) {
                                    messages[idx] = messages[idx].copy(
                                        inferenceMetrics = timer.buildMetrics()
                                    )
                                }
                                state.copy(
                                    messages = messages,
                                    isGenerating = false,
                                    liveStats = null
                                )
                            } else state
                        }

                        // Phase 2: Reactive safety net - check if context filled up during generation
                        if (inferenceRepository.getStopReason() == StopReason.CONTEXT_FULL) {
                            val state = _uiState.value
                            if (state is ChatUiState.Ready) {
                                performContextReset(state.messages)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                // Silently handle cancellation
            }
        }
    }

    private suspend fun performContextReset(messages: List<ChatMessage>) {
        // Create progress message with fixed ID
        val progressMessageId = "context_reset_${Clock.System.now()}"
        val progressMessage = ChatMessage(
            id = progressMessageId,
            role = MessageRole.System,
            text = "Chat summarization in progress"
        )
        
        // Add progress message to UI
        _uiState.update { state ->
            if (state is ChatUiState.Ready) {
                state.copy(messages = state.messages + progressMessage)
            } else state
        }
        
        try {
            // Filter out system messages for processing
            val nonSystemMessages = messages.filter { it.role != MessageRole.System }
            
            if (nonSystemMessages.isEmpty()) {
                // No conversation history, just reset with default system prompt
                inferenceRepository.resetContextWithSummary("", "")
                updateProgressMessage(progressMessageId, "Chat summarized")
                return
            }

            // Split into older messages and last exchange
            // Last exchange = last 2 messages (user + assistant), or last 1 if odd count
            val lastExchangeMessages = nonSystemMessages.takeLast(2)
            val olderMessages = nonSystemMessages.dropLast(2)
            
            // Build last exchange string
            val lastExchange = if (lastExchangeMessages.isNotEmpty()) {
                lastExchangeMessages.joinToString("\n") { "${it.role}: ${it.text}" }
            } else {
                ""
            }

            // Only summarize older messages if they exist
            val summary = if (olderMessages.isNotEmpty()) {
                val olderTranscript = olderMessages.joinToString("\n") { "${it.role}: ${it.text}" }
                val summaryBuilder = StringBuilder()
                
                try {
                    inferenceRepository.summarizeConversation(olderTranscript)
                        .collect { token ->
                            summaryBuilder.append(token)
                        }
                } catch (e: Exception) {
                    // Summarization failed - fall back to simple truncation
                    val recentMessages = olderMessages
                        .takeLast(4) // Keep last 2 pairs of older messages
                        .joinToString("\n") { "${it.role}: ${it.text.take(100)}" }
                    summaryBuilder.append("Previous messages:\n$recentMessages")
                }
                
                summaryBuilder.toString().trim()
            } else {
                ""
            }

            // Reset context with summary and last exchange
            val resetSuccess = inferenceRepository.resetContextWithSummary(summary, lastExchange)
            
            if (!resetSuccess) {
                // If reset with summary failed, try with just last exchange
                inferenceRepository.resetContextWithSummary("", lastExchange)
            }

            // Update progress message to completion
            updateProgressMessage(progressMessageId, "Chat summarized")
        } catch (e: Exception) {
            // If everything fails, at least reset with empty context
            try {
                inferenceRepository.resetContextWithSummary("", "")
            } catch (_: Exception) {
                // Give up
            }
            // Still update the progress message
            updateProgressMessage(progressMessageId, "Chat summarized")
        }
    }
    
    private fun updateProgressMessage(messageId: String, newText: String) {
        _uiState.update { state ->
            if (state is ChatUiState.Ready) {
                val messages = state.messages.toMutableList()
                val idx = messages.indexOfFirst { it.id == messageId }
                if (idx >= 0) {
                    messages[idx] = messages[idx].copy(text = newText)
                }
                state.copy(messages = messages)
            } else state
        }
    }

    fun cancelGeneration() {
        inferenceRepository.cancelGeneration()
        generationJob?.cancel()
    }
}
