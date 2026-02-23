package com.debanshu777.flash.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.debanshu777.flash.ui.components.ModelListItem
import com.debanshu777.flash.ui.components.SearchBar
import com.debanshu777.flash.ui.components.SearchModelListItem
import com.debanshu777.flash.ui.components.SortFilterChips
import com.debanshu777.flash.ui.viewmodel.ModelViewModel

@Composable
fun SearchScreen(
    viewModel: ModelViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResponse by viewModel.searchResponse.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val listParams by viewModel.listParams.collectAsState()
    val listResponse by viewModel.listResponse.collectAsState()
    val isListLoading by viewModel.isListLoading.collectAsState()
    val listError by viewModel.listError.collectAsState()

    val isSearchMode = searchQuery.isNotEmpty() || searchResponse != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = {
                if (searchQuery.isNotEmpty()) {
                    viewModel.performSearch()
                } else {
                    viewModel.loadModels()
                }
            }
        )

        if (isSearchMode) {
            if (searchResponse != null || searchError != null) {
                Column {
                    Text(
                        text = "Results for \"$searchQuery\" (${searchResponse?.modelsCount ?: 0} models)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            viewModel.clearSearch()
                        }
                    ) {
                        Text("← Back to Browse")
                    }
                }
            }
        } else {
            SortFilterChips(
                sort = listParams.sort,
                minParams = listParams.minParams,
                maxParams = listParams.maxParams,
                onSortChange = { viewModel.updateParams(sort = it) },
                onMinParamsChange = { viewModel.updateParams(minParams = it) },
                onMaxParamsChange = { viewModel.updateParams(maxParams = it) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isSearchMode -> {
                    when {
                        isSearchLoading -> CircularProgressIndicator()
                        searchError != null -> Text(
                            text = searchError ?: "Something went wrong. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        searchResponse?.models.isNullOrEmpty() -> Text(
                            text = "No models found for \"$searchQuery\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = searchResponse?.models?.filterNotNull() ?: emptyList(),
                                key = { it.id ?: it.hashCode().toString() }
                            ) { model ->
                                SearchModelListItem(
                                    model = model,
                                    onClick = { model.id?.let { id -> onNavigateToDetails(id) } }
                                )
                            }
                        }
                    }
                }

                else -> {
                    when {
                        isListLoading -> CircularProgressIndicator()
                        listError != null -> Text(
                            text = listError ?: "Something went wrong. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        listResponse?.models.isNullOrEmpty() -> Text(
                            text = "No models found. Tap Search to try.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = listResponse?.models?.filterNotNull() ?: emptyList(),
                                key = { it.id ?: it.hashCode().toString() }
                            ) { model ->
                                ModelListItem(
                                    model = model,
                                    onClick = { model.id?.let { id -> onNavigateToDetails(id) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
