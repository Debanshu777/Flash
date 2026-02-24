package com.debanshu777.flash.di

import com.debanshu777.flash.storage.AppDatabase
import com.debanshu777.flash.storage.LocalModelRepository
import com.debanshu777.huggingfacemanager.createHuggingFaceApi
import com.debanshu777.huggingfacemanager.download.DownloadManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformHuggingFaceModule: Module

val huggingFaceModule = module {
    includes(platformHuggingFaceModule)
    
    // DAO (database is provided by platform module)
    single { get<AppDatabase>().localModelDao() }
    
    // Repositories and managers
    single { LocalModelRepository(get()) }
    single { DownloadManager(get()) }
    
    // API
    single { createHuggingFaceApi() }
}
