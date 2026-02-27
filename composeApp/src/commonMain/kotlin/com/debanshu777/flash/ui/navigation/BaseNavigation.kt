package com.debanshu777.flash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.debanshu777.flash.ui.screens.ChatScreen
import com.debanshu777.flash.ui.screens.DetailsScreen
import com.debanshu777.flash.ui.screens.DownloadedModelsScreen
import com.debanshu777.flash.ui.screens.SearchScreen
import com.debanshu777.flash.ui.viewmodel.DownloadedModelsViewModel
import com.debanshu777.flash.ui.viewmodel.ModelViewModel
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
                entry(NavigableScreen.Search) {
                    val modelViewModel: ModelViewModel = koinViewModel()
                    SearchScreen(
                        viewModel = modelViewModel,
                        onNavigateToDetails = { backStack.add(NavigableScreen.Details(it)) },
                        onNavigateToDownloads = { backStack.add(NavigableScreen.DownloadedModels) },
                    )
                }
                entry(NavigableScreen.DownloadedModels) {
                    val downloadedModelsViewModel: DownloadedModelsViewModel = koinViewModel()
                    DownloadedModelsScreen(
                        viewModel = downloadedModelsViewModel,
                        onNavigateToDetails = { path, id ->
                            backStack.add(NavigableScreen.Chat(path, id))
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
                    ChatScreen(
                        modelPath = key.modelPath,
                        modelId = key.modelId,
                        onBack = { backStack.removeLastOrNull() },
                        storagePathProvider = storagePathProvider,
                    )
                }
            },
    )
}
