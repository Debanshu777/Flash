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
import com.debanshu777.flash.ui.screens.DetailsScreen
import com.debanshu777.flash.ui.screens.SearchScreen
import com.debanshu777.flash.ui.viewmodel.ModelViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val modelViewModel: ModelViewModel = koinViewModel()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Search) }
            when (val s = currentScreen) {
                is Screen.Search -> SearchScreen(
                    viewModel = modelViewModel,
                    onNavigateToDetails = { currentScreen = Screen.Details(it) }
                )
                is Screen.Details -> DetailsScreen(
                    viewModel = modelViewModel,
                    modelId = s.modelId,
                    onBack = { currentScreen = Screen.Search }
                )
            }
        }
    }
}
