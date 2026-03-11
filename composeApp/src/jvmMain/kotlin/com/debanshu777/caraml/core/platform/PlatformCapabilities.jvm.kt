package com.debanshu777.caraml.core.platform

actual object PlatformCapabilities {
    actual fun getDeviceHints(): DeviceHints {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        
        val perfCores = if (isMac) {
            detectMacPerformanceCores() ?: (totalCores / 2)
        } else {
            totalCores / 2
        }
        
        val totalMem = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        val maxCtx = calculateMaxContextSize(totalMem)
        
        return DeviceHints(
            performanceCoreCount = perfCores.coerceAtLeast(2),
            totalCoreCount = totalCores,
            availableMemoryMB = totalMem,
            gpuBackendCompiled = isMac,
            supportsMmap = true,
            maxContextSize = maxCtx,
        )
    }

    private fun calculateMaxContextSize(availMemMB: Long): Int {
        return when {
            availMemMB < 3072 -> 2048
            availMemMB < 6144 -> 4096
            availMemMB < 10240 -> 8192
            else -> 16384
        }
    }

    private fun detectMacPerformanceCores(): Int? {
        return try {
            val process = ProcessBuilder("sysctl", "-n", "hw.perflevel0.physicalcpu")
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            process.waitFor()
            
            if (process.exitValue() == 0) {
                output.toIntOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
