package com.debanshu777.flash.ui.navigation

sealed class Screen {
    data object Search : Screen()
    data class Details(val modelId: String) : Screen()
}
