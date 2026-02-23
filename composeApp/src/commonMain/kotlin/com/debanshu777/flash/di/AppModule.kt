package com.debanshu777.flash.di

import org.koin.dsl.module

val appModule = module {
    includes(huggingFaceModule, viewModelModule)
}
