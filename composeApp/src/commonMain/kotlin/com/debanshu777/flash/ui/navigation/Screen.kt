package com.debanshu777.flash.ui.navigation

sealed class Screen {
    data object Search : Screen()
    data object DownloadedModels : Screen()
    data class Details(val modelId: String) : Screen()
    data class Chat(val modelPath: String, val modelId: String) : Screen()
}
