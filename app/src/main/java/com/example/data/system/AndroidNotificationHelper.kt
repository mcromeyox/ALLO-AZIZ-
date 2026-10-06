package com.example.data.system

import android.Manifest
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
import com.example.MainActivity
import com.example.R

/**
 * AndroidNotificationHelper
 *
 * Dispatches real Android System Notifications with notification channels,
 * heads-up display, sound, and deep-link click back to the application.
 */
object AndroidNotificationHelper {

    const val CHANNEL_ORDERS_ID = "allo_aziz_orders_channel"
    const val CHANNEL_PROMOS_ID = "allo_aziz_promos_channel"

    private var channelsCreated = false

    fun initChannels(context: Context) {
        if (channelsCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // 1. Channel for critical Order status updates (Heads-up / High importance)
        val ordersChannel = NotificationChannel(
            CHANNEL_ORDERS_ID,
            "حالة الطلبات والتوصيل (Order Tracking)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات فورية لتحديثات حالة الطلب وموقع السائق"
            enableVibration(true)
            setShowBadge(true)
        }

        // 2. Channel for announcements & promotions
        val promosChannel = NotificationChannel(
            CHANNEL_PROMOS_ID,
            "العروض والتنبيهات (Promotions & Alerts)",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "العروض الترويجية والخصومات الخاصة"
        }

        notificationManager.createNotificationChannel(ordersChannel)
        notificationManager.createNotificationChannel(promosChannel)
        channelsCreated = true
    }

    /**
     * Posts a real Android system notification for order updates.
     */
    fun showOrderNotification(
        context: Context,
        orderId: String,
        title: String,
        body: String,
        notificationId: Int = (orderId.hashCode() and 0x7FFFFFFF)
    ) {
        initChannels(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRACK_ORDER_ID", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ORDERS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }

    /**
     * Posts a promotion or system announcement notification.
     */
    fun showPromoNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (System.currentTimeMillis().toInt() and 0x7FFFFFFF)
    ) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PROMOS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission revoked
        }
    }
}
