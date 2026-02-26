package com.debanshu777.flash.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.debanshu777.flash.ui.components.LocalModelListItem
import com.debanshu777.flash.ui.viewmodel.DownloadedModelsViewModel

@Composable
fun DownloadedModelsScreen(
    viewModel: DownloadedModelsViewModel,
    onNavigateToDetails: (modelPath: String, modelId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadedModels by viewModel.downloadedModels.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
        }

        Text(
            text = "My Downloads",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (downloadedModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No downloaded models yet.\nBrowse and download models to see them here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = downloadedModels,
                    key = { it.id }
                ) { model ->
                    LocalModelListItem(
                        model = model,
                        onClick = {
                            val resolvedPath = viewModel.getResolvedModelPath(model)
                            onNavigateToDetails(resolvedPath, model.modelId)
                        }
                    )
                }
            }
        }
    }
}
