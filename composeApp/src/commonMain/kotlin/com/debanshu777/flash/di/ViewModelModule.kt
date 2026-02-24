package com.debanshu777.flash.di

import com.debanshu777.flash.ui.viewmodel.ModelViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { 
        ModelViewModel(
            api = get(),
            localModelRepository = get(),
            downloadManager = get()
        ) 
    }
}
