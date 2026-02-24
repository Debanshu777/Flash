package com.debanshu777.flash.di

import com.debanshu777.flash.storage.AppDatabase
import com.debanshu777.flash.storage.getDatabaseBuilder
import com.debanshu777.flash.storage.getRoomDatabase
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import com.debanshu777.huggingfacemanager.download.IosStoragePathProvider
import org.koin.dsl.module

actual val platformHuggingFaceModule = module {
    // Storage path provider (iOS-specific)
    single<StoragePathProvider> { IosStoragePathProvider() }
    
    // Database (iOS-specific)
    single<AppDatabase> {
        val pathProvider = get<StoragePathProvider>()
        val dbPath = pathProvider.getDatabasePath()
        val builder = getDatabaseBuilder(dbPath)
        getRoomDatabase(builder)
    }
}
