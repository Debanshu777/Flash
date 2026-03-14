package com.debanshu777.caraml.core.platform

import android.app.ActivityManager
import android.content.Context
import java.io.File

private const val TAG = "DeviceCapabilities"

class AndroidDeviceCapabilities(
    private val context: Context,
) : DeviceCapabilities {

    private val cachedHints: DeviceHints by lazy { computeHints() }

    override fun getDeviceHints(): DeviceHints = cachedHints

    private fun computeHints(): DeviceHints {
        val totalCores = Runtime.getRuntime().availableProcessors()
        return DeviceHints(
            performanceCoreCount = detectPerformanceCores(totalCores),
            totalCoreCount = totalCores,
            memoryBudgetMB = getDeviceMemoryMB(),
            // TODO: Enable Vulkan GPU offloading. Requires:
            //  1. Native llama.cpp library compiled with -DGGML_VULKAN=ON
            //  2. Runtime check: PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL >= 1
            //  3. Gradle BuildConfig flag (VULKAN_ENABLED) to gate at build time
            gpuBackendAvailable = false,
        )
    }

    private fun getDeviceMemoryMB(): Long {
        return try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalMB = memInfo.totalMem / (1024 * 1024)
            (totalMB * 0.70).toLong()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Memory detection failed, using fallback 2048MB", e)
            2048L
        }
    }

    private fun detectPerformanceCores(totalCores: Int): Int {
        val capacityResult = detectViaCapacity(totalCores)
        if (capacityResult != null) return capacityResult

        val freqResult = detectViaFrequency(totalCores)
        if (freqResult != null) return freqResult

        val fallback = (totalCores / 2).coerceIn(2, totalCores - 1)
        AppLogger.w(TAG, "Perf core detection: all strategies failed, using fallback=$fallback")
        return fallback
    }

    private fun detectViaCapacity(totalCores: Int): Int? {
        return try {
            val capacities = (0 until totalCores).mapNotNull { i ->
                try {
                    File("/sys/devices/system/cpu/cpu$i/cpu_capacity")
                        .readText().trim().toIntOrNull()
                } catch (_: Exception) { null }
            }
            if (capacities.isEmpty()) return null

            val maxCap = capacities.max()
            val threshold = (maxCap * 0.70).toInt()
            val perfCount = capacities.count { it >= threshold }
            perfCount.coerceIn(2, totalCores - 1)
        } catch (e: Exception) {
            AppLogger.w(TAG, "cpu_capacity detection failed", e)
            null
        }
    }

    private fun detectViaFrequency(totalCores: Int): Int? {
        return try {
            val frequencies = (0 until totalCores).mapNotNull { i ->
                try {
                    File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                        .readText().trim().toLongOrNull()
                } catch (_: Exception) { null }
            }
            if (frequencies.isEmpty()) return null

            val maxFreq = frequencies.max()
            val threshold = (maxFreq * 0.8).toLong()
            val perfCoreCount = frequencies.count { it >= threshold }
            perfCoreCount.coerceIn(2, totalCores - 1)
        } catch (e: Exception) {
            AppLogger.w(TAG, "cpufreq detection failed", e)
            null
        }
    }
}
