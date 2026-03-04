package com.debanshu777.caraml.di

import org.koin.dsl.module

val appModule = module {
    includes(huggingFaceModule, viewModelModule)
}
