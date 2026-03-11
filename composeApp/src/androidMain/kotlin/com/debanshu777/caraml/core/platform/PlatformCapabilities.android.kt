package com.debanshu777.caraml.core.platform

import android.app.ActivityManager
import android.content.Context
import org.koin.mp.KoinPlatform
import java.io.File

actual object PlatformCapabilities {
    actual fun getDeviceHints(): DeviceHints {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val perfCores = detectPerformanceCores(totalCores)
        val memInfo = getAvailableMemory()
        val maxCtx = calculateMaxContextSize(memInfo)
        
        return DeviceHints(
            performanceCoreCount = perfCores,
            totalCoreCount = totalCores,
            availableMemoryMB = memInfo,
            gpuBackendCompiled = false,
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

    private fun detectPerformanceCores(totalCores: Int): Int {
        try {
            val frequencies = (0 until totalCores).mapNotNull { i ->
                try {
                    File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                        .readText().trim().toLongOrNull()
                } catch (_: Exception) {
                    null
                }
            }
            
            if (frequencies.isEmpty()) {
                return totalCores.coerceAtMost(4)
            }
            
            val maxFreq = frequencies.max()
            val threshold = (maxFreq * 0.8).toLong()
            val perfCoreCount = frequencies.count { it >= threshold }
            
            return perfCoreCount.coerceIn(2, totalCores - 1)
        } catch (_: Exception) {
            return totalCores.coerceAtMost(4)
        }
    }

    private fun getAvailableMemory(): Long {
        return try {
            val context = KoinPlatform.getKoin().get<Context>()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            memInfo.availMem / (1024 * 1024)
        } catch (_: Exception) {
            2048L
        }
    }
}
