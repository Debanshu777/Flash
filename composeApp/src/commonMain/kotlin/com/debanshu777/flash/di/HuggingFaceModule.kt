package com.debanshu777.flash.di

import com.debanshu777.huggingfacemanager.createHuggingFaceApi
import org.koin.dsl.module

val huggingFaceModule = module {
    single { createHuggingFaceApi() }
}
