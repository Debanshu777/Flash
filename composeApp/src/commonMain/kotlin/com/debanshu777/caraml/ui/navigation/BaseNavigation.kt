package com.debanshu777.caraml.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.debanshu777.caraml.ui.screens.ChatScreen
import com.debanshu777.caraml.ui.screens.DetailsScreen
import com.debanshu777.caraml.ui.screens.SearchScreen
import com.debanshu777.caraml.ui.screens.StandaloneChatScreen
import com.debanshu777.caraml.ui.viewmodel.ChatViewModel
import com.debanshu777.caraml.ui.viewmodel.DownloadedModelsViewModel
import com.debanshu777.caraml.ui.viewmodel.ModelViewModel
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationHost(
    modifier: Modifier,
    backStack: NavBackStack<NavKey>,
    storagePathProvider: StoragePathProvider = koinInject(),
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry(NavigableScreen.Home) {
                    val chatViewModel: ChatViewModel = koinInject()
                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateToSearch = { backStack.add(NavigableScreen.Search) },
                        storagePathProvider = storagePathProvider,
                    )
                }
                entry(NavigableScreen.Search) {
                    val modelViewModel: ModelViewModel = koinViewModel()
                    val downloadedModelsViewModel: DownloadedModelsViewModel = koinViewModel()
                    val chatViewModel: ChatViewModel = koinInject()
                    SearchScreen(
                        modelViewModel = modelViewModel,
                        downloadedModelsViewModel = downloadedModelsViewModel,
                        onNavigateToDetails = { backStack.add(NavigableScreen.Details(it)) },
                        onSelectModelAndGoBack = { model ->
                            chatViewModel.selectModel(model)
                            backStack.removeLastOrNull()
                        },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<NavigableScreen.Details> { key ->
                    val modelViewModel: ModelViewModel = koinViewModel()
                    DetailsScreen(
                        viewModel = modelViewModel,
                        modelId = key.modelId,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<NavigableScreen.Chat> { key ->
                    StandaloneChatScreen(
                        modelPath = key.modelPath,
                        modelId = key.modelId,
                        onBack = { backStack.removeLastOrNull() },
                        storagePathProvider = storagePathProvider,
                    )
                }
            },
    )
}
