package com.debanshu777.flash.di

import com.debanshu777.flash.ui.viewmodel.ModelViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ModelViewModel(get()) }
}
