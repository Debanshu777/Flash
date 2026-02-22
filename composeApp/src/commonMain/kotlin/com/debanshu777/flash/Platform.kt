package com.debanshu777.flash

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform