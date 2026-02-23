package com.debanshu777.flash

import androidx.compose.ui.window.ComposeUIViewController
import com.debanshu777.flash.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}