package com.debanshu777.caraml.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.Foundation.NSProcessInfo
import platform.darwin.sysctlbyname
import platform.posix.uint64_tVar

@OptIn(ExperimentalForeignApi::class)
actual object PlatformCapabilities {
    actual fun getDeviceHints(): DeviceHints {
        val perfCores = sysctlPerflevel0PhysicalCpu()
        val totalCores = NSProcessInfo.processInfo.activeProcessorCount.toInt()
        val availMem = getAvailableMemoryMB()
        val isSimulator = isRunningOnSimulator()
        val maxCtx = calculateMaxContextSize(availMem, isSimulator)

        return DeviceHints(
            performanceCoreCount = perfCores.coerceAtLeast(2),
            totalCoreCount = totalCores,
            availableMemoryMB = availMem,
            gpuBackendCompiled = !isSimulator,
            supportsMmap = true,
            maxContextSize = maxCtx,
        )
    }

    private fun isRunningOnSimulator(): Boolean {
        return NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    }

    private fun calculateMaxContextSize(availMemMB: Long, isSimulator: Boolean): Int {
        if (isSimulator) {
            return 4096
        }
        return when {
            availMemMB < 1024 -> 512
            availMemMB < 2048 -> 1024
            availMemMB < 3072 -> 2048
            availMemMB < 6144 -> 4096
            availMemMB < 10240 -> 8192
            else -> 16384
        }
    }

    private fun sysctlPerflevel0PhysicalCpu(): Int {
        return memScoped {
            val value = alloc<IntVar>()
            val size = alloc<ULongVar>()
            size.value = sizeOf<IntVar>().toULong()
            val result = sysctlbyname(
                "hw.perflevel0.physicalcpu",
                value.ptr,
                size.ptr,
                null,
                0u
            )
            if (result == 0) {
                value.value
            } else {
                val totalCores = NSProcessInfo.processInfo.activeProcessorCount.toInt()
                (totalCores / 2).coerceAtLeast(2)
            }
        }
    }

    private fun getAvailableMemoryMB(): Long {
        return memScoped {
            val value = alloc<uint64_tVar>()
            val size = alloc<ULongVar>()
            size.value = sizeOf<uint64_tVar>().toULong()
            val result = sysctlbyname(
                "hw.memsize",
                value.ptr,
                size.ptr,
                null,
                0u
            )
            if (result == 0) {
                val totalMemBytes = value.value.toLong()
                (totalMemBytes * 0.7).toLong() / (1024 * 1024)
            } else {
                4096L
            }
        }
    }
}
