package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidLiveUpdateNowPlayingSurfacePublisherTest {

    @Test
    fun candidateUsesVerifiedM0BShape() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val publisher = AndroidLiveUpdateNowPlayingSurfacePublisher(
            context = context,
            policy = NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED
        )
        publisher.ensureChannel()

        val notification = publisher.buildCandidateNotification(
            event = NowPlayingSurfaceEvent(
                title = "Song",
                artist = "Artist",
                timeoutMillis = 60_000L
            ),
            requestPromotion = true
        )
        val channel = context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(AndroidLiveUpdateNowPlayingSurfacePublisher.CHANNEL_ID)

        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertTrue(NotificationCompat.isRequestPromotedOngoing(notification))
        assertTrue((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0)
        assertEquals(Notification.CATEGORY_STATUS, notification.category)
        assertEquals(Notification.VISIBILITY_PUBLIC, notification.visibility)
        assertEquals(NotificationCompat.PRIORITY_HIGH, notification.priority)
        assertEquals(60_000L, NotificationCompat.getTimeoutAfter(notification))
        assertNull(notification.contentView)
        assertNull(notification.bigContentView)
        assertNull(notification.headsUpContentView)
    }
}
