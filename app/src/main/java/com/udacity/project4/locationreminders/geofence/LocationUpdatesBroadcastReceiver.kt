package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationAvailability

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
            if (!locationAvailability.isLocationAvailable) {
                NotificationHelper(context).locationLostNotification()
            }
        }
    }
}