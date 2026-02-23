package com.debanshu777.flash.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.debanshu777.flash.ui.components.ModelDetailContent
import com.debanshu777.flash.ui.viewmodel.ModelViewModel

@Composable
fun DetailsScreen(
    viewModel: ModelViewModel,
    modelId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modelDetail by viewModel.modelDetail.collectAsState()
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val detailError by viewModel.detailError.collectAsState()

    LaunchedEffect(modelId) {
        viewModel.loadDetail(modelId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDetailLoading -> CircularProgressIndicator()
                detailError != null -> Text(
                    text = detailError ?: "Could not load model details. Please try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                modelDetail != null -> ModelDetailContent(
                    model = modelDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
