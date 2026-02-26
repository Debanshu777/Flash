package com.debanshu777.flash

import android.app.Application
import com.debanshu777.flash.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class FlashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FlashApplication)
            androidLogger(Level.DEBUG)
        }
    }
}
