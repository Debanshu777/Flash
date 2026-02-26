package com.debanshu777.flash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.debanshu777.flash.ui.navigation.Screen
import com.debanshu777.flash.ui.screens.ChatScreen
import com.debanshu777.flash.ui.screens.DetailsScreen
import com.debanshu777.flash.ui.screens.DownloadedModelsScreen
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import com.debanshu777.flash.ui.screens.SearchScreen
import com.debanshu777.flash.ui.viewmodel.DownloadedModelsViewModel
import com.debanshu777.flash.ui.viewmodel.ModelViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    storagePathProvider: StoragePathProvider = koinInject()
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val modelViewModel: ModelViewModel = koinViewModel()
            val downloadedModelsViewModel: DownloadedModelsViewModel = koinViewModel()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Search) }
            when (val s = currentScreen) {
                is Screen.Search -> SearchScreen(
                    viewModel = modelViewModel,
                    onNavigateToDetails = { currentScreen = Screen.Details(it) },
                    onNavigateToDownloads = { currentScreen = Screen.DownloadedModels }
                )
                is Screen.DownloadedModels -> DownloadedModelsScreen(
                    viewModel = downloadedModelsViewModel,
                    onNavigateToDetails = { path, id -> currentScreen = Screen.Chat(path, id) },
                    onBack = { currentScreen = Screen.Search }
                )
                is Screen.Details -> DetailsScreen(
                    viewModel = modelViewModel,
                    modelId = s.modelId,
                    onBack = { currentScreen = Screen.Search }
                )
                is Screen.Chat -> ChatScreen(
                    modelPath = s.modelPath,
                    modelId = s.modelId,
                    onBack = { currentScreen = Screen.DownloadedModels },
                    storagePathProvider = storagePathProvider
                )
            }
        }
    }
}
