package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingSurfacePolicyTest {

    private val eligible = NowPlayingSurfaceCapabilities(
        notificationsEnabled = true,
        channelBlocked = false,
        hasPromotableCharacteristics = true,
        canPostPromotedNotifications = true,
        postNotificationsGranted = true
    )

    @Test
    fun standardOnlyNeverRequestsPromotion() {
        assertFalse(
            NowPlayingSurfacePolicy.STANDARD_ONLY.shouldRequestPromotion(eligible)
        )
    }

    @Test
    fun experimentalPromotedRequestsPromotionOnlyWhenFullyEligible() {
        assertTrue(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(eligible)
        )

        assertFalse(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(
                eligible.copy(canPostPromotedNotifications = false)
            )
        )

        assertFalse(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(
                eligible.copy(channelBlocked = true)
            )
        )
    }

    @Test
    fun experimentalPromotedRequiresNotificationPermission() {
        assertFalse(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(
                eligible.copy(postNotificationsGranted = false)
            )
        )
    }

    @Test
    fun experimentalPromotedRequiresEnabledNotificationsAndPromotableCharacteristics() {
        assertFalse(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(
                eligible.copy(notificationsEnabled = false)
            )
        )
        assertFalse(
            NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED.shouldRequestPromotion(
                eligible.copy(hasPromotableCharacteristics = false)
            )
        )
    }

    @Test
    fun eventTimeoutIsBoundedAndHasSixtySecondFallback() {
        assertEquals(
            60_000L,
            NowPlayingSurfaceEvent(title = "Song", artist = "Artist").boundedTimeoutMillis()
        )
        assertEquals(
            30_000L,
            NowPlayingSurfaceEvent(
                title = "Song",
                artist = "Artist",
                timeoutMillis = 1L
            ).boundedTimeoutMillis()
        )
        assertEquals(
            600_000L,
            NowPlayingSurfaceEvent(
                title = "Song",
                artist = "Artist",
                timeoutMillis = Long.MAX_VALUE
            ).boundedTimeoutMillis()
        )
    }
}
