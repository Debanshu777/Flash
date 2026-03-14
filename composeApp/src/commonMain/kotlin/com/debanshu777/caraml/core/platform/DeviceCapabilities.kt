package com.debanshu777.caraml.core.platform

data class DeviceHints(
    val performanceCoreCount: Int,
    val totalCoreCount: Int,
    val memoryBudgetMB: Long,
    val gpuBackendAvailable: Boolean,
)

interface DeviceCapabilities {
    fun getDeviceHints(): DeviceHints
}
