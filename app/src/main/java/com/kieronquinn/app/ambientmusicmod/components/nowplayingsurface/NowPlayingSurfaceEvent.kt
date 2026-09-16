package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

data class NowPlayingSurfaceEvent(
    val title: String,
    val artist: String,
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) {
    fun boundedTimeoutMillis(): Long {
        return timeoutMillis.coerceIn(MIN_TIMEOUT_MILLIS, MAX_TIMEOUT_MILLIS)
    }

    companion object {
        const val MIN_TIMEOUT_MILLIS = 30_000L
        const val MAX_TIMEOUT_MILLIS = 600_000L
        const val DEFAULT_TIMEOUT_MILLIS = 60_000L
    }
}
