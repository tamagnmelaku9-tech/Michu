package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "michu_notifications"
    private const val CHANNEL_NAME = "Michu Notifications"
    private const val CHANNEL_DESC = "Notifications for order updates and chat messages"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created successfully")
        }
    }

    fun triggerNotification(context: Context, title: String, message: String) {
        try {
            // First ensure the channel is created
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Using system default notification icons or the app icon
            val iconResId = R.drawable.img_app_icon

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500))
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            // check permission (on Android 13+ we should check permission but even if it's not granted, 
            // the API call is safe and won't crash - it just won't display. We still try to post)
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d(TAG, "Notification triggered: $title - $message")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: permission POST_NOTIFICATIONS may be missing", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger notification", e)
        }
    }
}
