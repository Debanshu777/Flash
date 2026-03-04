package com.debanshu777.caraml.platform

expect object PlatformPaths {
    fun getNativeLibDir(): String
    fun getDefaultGpuLayers(): Int
}
