package com.debanshu777.flash.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NavigableScreen : NavKey {
    @Serializable
    data object Search : NavigableScreen

    @Serializable
    data object DownloadedModels : NavigableScreen

    @Serializable
    data class Details(val modelId: String) : NavigableScreen

    @Serializable
    data class Chat(val modelPath: String, val modelId: String) : NavigableScreen
}
