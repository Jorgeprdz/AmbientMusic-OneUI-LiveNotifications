package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

data class NowPlayingSurfaceCapabilities(
    val notificationsEnabled: Boolean,
    val channelBlocked: Boolean,
    val hasPromotableCharacteristics: Boolean,
    val canPostPromotedNotifications: Boolean,
    val postNotificationsGranted: Boolean = true
)

enum class NowPlayingSurfacePolicy {
    STANDARD_ONLY,
    EXPERIMENTAL_PROMOTED;

    fun shouldRequestPromotion(capabilities: NowPlayingSurfaceCapabilities): Boolean {
        return this == EXPERIMENTAL_PROMOTED &&
            capabilities.postNotificationsGranted &&
            capabilities.notificationsEnabled &&
            !capabilities.channelBlocked &&
            capabilities.hasPromotableCharacteristics &&
            capabilities.canPostPromotedNotifications
    }
}
