package com.kieronquinn.app.ambientmusicmod.debug

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NowPlayingSurfaceProbeTest {

    @Test
    fun candidateUsesOnlyPromotableStandardNotificationShape() {
        assumeTrue(Build.VERSION.SDK_INT >= 36)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val probe = NowPlayingSurfaceProbe(context)
        probe.ensureChannel()

        val notification = probe.buildCandidateNotification()

        assertTrue(NotificationCompat.isRequestPromotedOngoing(notification))
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertEquals(Notification.CATEGORY_STATUS, notification.category)
        assertEquals(Notification.VISIBILITY_PUBLIC, notification.visibility)
        assertEquals(
            NowPlayingSurfaceProbe.TIMEOUT_MILLIS,
            NotificationCompat.getTimeoutAfter(notification)
        )
        assertEquals("", NotificationCompat.getShortCriticalText(notification))
        assertTrue(NotificationCompat.hasPromotableCharacteristics(notification))
        assertNull(notification.contentView)
        assertNull(notification.bigContentView)
        assertNull(notification.headsUpContentView)
    }
}
