package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

/*
    This service requests the user's location so that the geofences
    are triggered without the need to have the application or the map open.
*/
class LocationUpdaterService : Service() {

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest().apply {
            interval = 20000 // Minute
            fastestInterval = 10000 // Half minute
            maxWaitTime = 30000 // Minute and a half
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)

        startForeground(1, NotificationHelper(this).backgroundLocationNotification())

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}