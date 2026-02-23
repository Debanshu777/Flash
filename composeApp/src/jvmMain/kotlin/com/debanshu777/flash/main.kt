package com.debanshu777.flash

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.debanshu777.flash.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Flash",
        ) {
            App()
        }
    }
}