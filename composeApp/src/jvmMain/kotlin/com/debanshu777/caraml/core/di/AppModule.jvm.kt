package com.debanshu777.caraml.core.di

import com.debanshu777.caraml.core.platform.DeviceCapabilities
import com.debanshu777.caraml.core.platform.JvmDeviceCapabilities
import com.debanshu777.caraml.core.storage.AppDatabase
import com.debanshu777.caraml.core.storage.getDatabaseBuilder
import com.debanshu777.caraml.core.storage.getRoomDatabase
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import com.debanshu777.huggingfacemanager.download.JvmStoragePathProvider
import org.koin.dsl.module

actual val platformHuggingFaceModule = module {
    single<StoragePathProvider> { JvmStoragePathProvider() }

    single<DeviceCapabilities> { JvmDeviceCapabilities() }

    single<AppDatabase> {
        val pathProvider = get<StoragePathProvider>()
        val dbPath = pathProvider.getDatabasePath()
        val builder = getDatabaseBuilder(dbPath)
        getRoomDatabase(builder)
    }
}
