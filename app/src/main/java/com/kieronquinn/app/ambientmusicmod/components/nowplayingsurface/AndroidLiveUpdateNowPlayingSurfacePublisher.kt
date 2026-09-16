package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kieronquinn.app.ambientmusicmod.R
import com.kieronquinn.app.ambientmusicmod.ui.activities.MainActivity

interface NowPlayingSurfacePublisher {
    fun publish(event: NowPlayingSurfaceEvent)
    fun clear()
}

class AndroidLiveUpdateNowPlayingSurfacePublisher(
    context: Context,
    private val policy: NowPlayingSurfacePolicy = NowPlayingSurfacePolicy.STANDARD_ONLY
): NowPlayingSurfacePublisher {

    companion object {
        const val CHANNEL_ID = "now_playing_surface_v1"
        const val NOTIFICATION_ID = 0x4E50
    }

    private val context = context.applicationContext
    private val notificationManager =
        this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationManagerCompat = NotificationManagerCompat.from(this.context)

    fun ensureChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Now Playing live update",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recognised music from Ambient Music Mod"
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    fun buildCandidateNotification(
        event: NowPlayingSurfaceEvent,
        requestPromotion: Boolean
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fab_recognise)
            .setContentTitle(event.title)
            .setContentText(event.artist)
            .setSubText("Ambient Music Mod")
            .setContentIntent(contentIntent)
            .setCategory(Notification.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSilent(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setShortCriticalText("")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(event.title)
                    .bigText(event.artist)
            )
            .setTimeoutAfter(event.boundedTimeoutMillis())
            .setRequestPromotedOngoing(requestPromotion)
            .build()
    }

    override fun publish(event: NowPlayingSurfaceEvent) {
        ensureChannel()
        val promotionCandidate = buildCandidateNotification(event, requestPromotion = true)
        val capabilities = readCapabilities(promotionCandidate)
        val requestPromotion = policy.shouldRequestPromotion(capabilities)
        if (!capabilities.postNotificationsGranted ||
            !capabilities.notificationsEnabled ||
            capabilities.channelBlocked
        ) return
        notificationManagerCompat.notify(
            NOTIFICATION_ID,
            buildCandidateNotification(event, requestPromotion)
        )
    }

    override fun clear() {
        notificationManagerCompat.cancel(NOTIFICATION_ID)
    }

    private fun readCapabilities(candidate: Notification): NowPlayingSurfaceCapabilities {
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        return NowPlayingSurfaceCapabilities(
            notificationsEnabled = notificationManagerCompat.areNotificationsEnabled(),
            channelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE,
            hasPromotableCharacteristics = NotificationCompat.hasPromotableCharacteristics(candidate),
            canPostPromotedNotifications = Build.VERSION.SDK_INT >= 36 &&
                notificationManagerCompat.canPostPromotedNotifications(),
            postNotificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
}
