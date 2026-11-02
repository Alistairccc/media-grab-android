package com.joma.mediagrab.core.util

import java.util.Locale

object Formatters {
    fun duration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        return String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    fun sizeMb(bytes: Long): Float = bytes / 1_000_000f

    fun speedMb(bytesPerSecond: Long): Float = bytesPerSecond / 1_000_000f
}
