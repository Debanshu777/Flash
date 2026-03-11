package com.debanshu777.caraml.core.platform

data class DeviceHints(
    val performanceCoreCount: Int,
    val totalCoreCount: Int,
    val availableMemoryMB: Long,
    val gpuBackendCompiled: Boolean,
    val supportsMmap: Boolean,
    val maxContextSize: Int,
)

expect object PlatformCapabilities {
    fun getDeviceHints(): DeviceHints
}
