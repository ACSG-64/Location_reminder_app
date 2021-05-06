package com.udacity.project4.locationreminders.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "Geofences notification channel"

    fun backgroundLocationNotification() : Notification {
        createNotificationChannel()
        /* Notification */
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(context.getString(R.string.location_notification_title))
            .setContentText(context.getString(R.string.location_notification_text))
            .build()
    }

    fun locationLostNotification(){
        createNotificationChannel()
        val NOTIFICATION_ID = 12

        /* Intents */
        val intentToMain = Intent(context, AuthenticationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentToGuide = PendingIntent.getActivity(context, 0, intentToMain, PendingIntent.FLAG_UPDATE_CURRENT)

        /* Notification */
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.location_lost_notification_title))
            .setContentText(context.getString(R.string.location_lost_notification_text))
            .setContentIntent(pendingIntentToGuide)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifier of tour destination reached",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}