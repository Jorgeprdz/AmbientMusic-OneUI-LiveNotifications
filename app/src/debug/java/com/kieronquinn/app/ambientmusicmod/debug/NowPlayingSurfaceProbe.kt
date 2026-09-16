package com.kieronquinn.app.ambientmusicmod.debug

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kieronquinn.app.ambientmusicmod.R

/**
 * Developer-only M0-B probe for Android promoted ongoing notifications.
 *
 * This intentionally has no Samsung-private API, MediaSession, Shizuku dependency,
 * notification listener, custom RemoteViews or recognition-engine dependency.
 */
internal class NowPlayingSurfaceProbe(private val context: Context) {

    companion object {
        // New channel id is intentional: Android does not allow an existing channel's
        // importance to be raised after creation.
        const val CHANNEL_ID = "now_playing_surface_m0b_v1"
        const val NOTIFICATION_ID = 0x4D30
        const val TIMEOUT_MILLIS = 60_000L
        private const val PROMOTED_PERMISSION = "android.permission.POST_PROMOTED_NOTIFICATIONS"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        val current = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (current != null) return
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "M0-B Live Update probe",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Finite public-API probe for Samsung live-notification eligibility"
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    fun buildCandidateNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, NowPlayingSurfaceProbeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fab_recognise)
            .setContentTitle("M0-B Live Update probe")
            .setContentText("Ambient Music Mod — public Android API")
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
                    .setBigContentTitle("M0-B Live Update probe")
                    .bigText("Ambient Music Mod — public Android API")
            )
            .setTimeoutAfter(TIMEOUT_MILLIS)
            .setRequestPromotedOngoing(true)
            .build()
    }

    fun post(): ProbeSnapshot {
        ensureChannel()
        val candidate = buildCandidateNotification()
        val prePost = snapshot(candidate)
        if (prePost.postNotificationsGranted && prePost.notificationsEnabled && !prePost.channelBlocked) {
            notificationManagerCompat.notify(NOTIFICATION_ID, candidate)
        }
        return snapshot(candidate)
    }

    fun cancel() {
        notificationManagerCompat.cancel(NOTIFICATION_ID)
    }

    fun hasPostNotificationsPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun openPromotionSettings(): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        if (intent.resolveActivity(context.packageManager) == null) return false
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    fun snapshot(candidate: Notification = buildCandidateNotification()): ProbeSnapshot {
        ensureChannel()
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        val active = notificationManager.activeNotifications
            .firstOrNull { it.id == NOTIFICATION_ID }
            ?.notification
        val promotedBySystem = if (Build.VERSION.SDK_INT >= 36 && active != null) {
            active.flags and Notification.FLAG_PROMOTED_ONGOING != 0
        } else {
            false
        }
        return ProbeSnapshot(
            sdkInt = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            postNotificationsGranted = hasPostNotificationsPermission(),
            promotedPermissionDeclared = isPromotedPermissionDeclared(),
            notificationsEnabled = notificationManagerCompat.areNotificationsEnabled(),
            channelExists = channel != null,
            channelImportance = channel?.importance,
            channelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE,
            promotableCharacteristics = NotificationCompat.hasPromotableCharacteristics(candidate),
            canPostPromotedNotifications = if (Build.VERSION.SDK_INT >= 36) {
                notificationManagerCompat.canPostPromotedNotifications()
            } else {
                false
            },
            promotionSettingsResolvable = promotionSettingsResolvable(),
            requestPromotedOngoing = NotificationCompat.isRequestPromotedOngoing(candidate),
            notificationActive = active != null,
            systemAssignedPromotedOngoing = promotedBySystem
        )
    }

    private fun isPromotedPermissionDeclared(): Boolean {
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        return packageInfo.requestedPermissions?.contains(PROMOTED_PERMISSION) == true
    }

    private fun promotionSettingsResolvable(): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        return intent.resolveActivity(context.packageManager) != null
    }
}

internal data class ProbeSnapshot(
    val sdkInt: Int,
    val release: String,
    val manufacturer: String,
    val model: String,
    val postNotificationsGranted: Boolean,
    val promotedPermissionDeclared: Boolean,
    val notificationsEnabled: Boolean,
    val channelExists: Boolean,
    val channelImportance: Int?,
    val channelBlocked: Boolean,
    val promotableCharacteristics: Boolean,
    val canPostPromotedNotifications: Boolean,
    val promotionSettingsResolvable: Boolean,
    val requestPromotedOngoing: Boolean,
    val notificationActive: Boolean,
    val systemAssignedPromotedOngoing: Boolean
) {
    fun asText(): String = buildString {
        appendLine("===== AMBIENT MUSIC MOD — M0-B LIVE UPDATE PROBE =====")
        appendLine("device=$manufacturer $model")
        appendLine("android=$release sdk=$sdkInt")
        appendLine("POST_NOTIFICATIONS=$postNotificationsGranted")
        appendLine("POST_PROMOTED_NOTIFICATIONS_DECLARED=$promotedPermissionDeclared")
        appendLine("NOTIFICATIONS_ENABLED=$notificationsEnabled")
        appendLine("CHANNEL_EXISTS=$channelExists")
        appendLine("CHANNEL_IMPORTANCE=${channelImportance ?: "missing"}")
        appendLine("CHANNEL_BLOCKED=$channelBlocked")
        appendLine("REQUEST_PROMOTED_ONGOING=$requestPromotedOngoing")
        appendLine("HAS_PROMOTABLE_CHARACTERISTICS=$promotableCharacteristics")
        appendLine("CAN_POST_PROMOTED_NOTIFICATIONS=$canPostPromotedNotifications")
        appendLine("PROMOTION_SETTINGS_RESOLVABLE=$promotionSettingsResolvable")
        appendLine("NOTIFICATION_ACTIVE=$notificationActive")
        appendLine("FLAG_PROMOTED_ONGOING=$systemAssignedPromotedOngoing")
        appendLine()
        appendLine(
            when {
                sdkInt < 36 -> "M0B=UNSUPPORTED_PLATFORM_API"
                !postNotificationsGranted -> "M0B=POST_NOTIFICATIONS_PERMISSION_REQUIRED"
                !notificationsEnabled -> "M0B=APP_NOTIFICATIONS_DISABLED"
                channelBlocked -> "M0B=CHANNEL_BLOCKED"
                !promotableCharacteristics -> "M0B=STRUCTURAL_FAIL"
                !canPostPromotedNotifications -> "M0B=PROMOTION_DISABLED_OR_OEM_INELIGIBLE"
                !systemAssignedPromotedOngoing -> "M0B=POSTED_AWAITING_OR_NOT_PROMOTED"
                else -> "M0B=PLATFORM_PROMOTED_VISUAL_NOW_BAR_CHECK_REQUIRED"
            }
        )
        appendLine("A real Samsung Now Bar PASS still requires visual verification.")
    }
}
