package com.debanshu777.caraml.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu777.caraml.storage.localModel.LocalModelEntity
import com.debanshu777.caraml.storage.localModel.LocalModelRepository
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val localModelRepository: LocalModelRepository,
    private val storagePathProvider: StoragePathProvider
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
    val selectedModel: StateFlow<LocalModelEntity?> = _selectedModel

    init {
        viewModelScope.launch {
            allDownloadedModels.collect { models ->
                if (_selectedModel.value == null && models.isNotEmpty()) {
                    _selectedModel.value = models.first()
                }
            }
        }
    }

    fun selectModel(model: LocalModelEntity) {
        _selectedModel.value = model
        viewModelScope.launch {
            localModelRepository.incrementUsageCount(model.modelId, model.filename)
        }
    }

    fun getResolvedModelPath(model: LocalModelEntity): String {
        if (model.localPath.isNotBlank() && storagePathProvider.fileExists(model.localPath)) {
            return model.localPath
        }
        val dir = storagePathProvider.getModelsStorageDirectory(model.modelId)
        return "$dir/${model.filename}"
    }
}
