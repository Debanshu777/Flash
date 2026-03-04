package com.debanshu777.caraml.di

import com.debanshu777.caraml.ui.viewmodel.DownloadedModelsViewModel
import com.debanshu777.caraml.ui.viewmodel.ModelViewModel
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
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
    viewModel { 
        DownloadedModelsViewModel(
            localModelRepository = get(),
            storagePathProvider = get()
        ) 
    }
}
