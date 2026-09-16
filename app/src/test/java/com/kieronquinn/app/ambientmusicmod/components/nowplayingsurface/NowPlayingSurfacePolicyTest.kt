package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingSurfacePolicyTest {

    private val eligible = NowPlayingSurfaceCapabilities(
        notificationsEnabled = true,
        channelBlocked = false,
        hasPromotableCharacteristics = true,
        canPostPromotedNotifications = true
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
}
