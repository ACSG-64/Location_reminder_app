package com.udacity.project4.locationreminders.geofence

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

fun createGeofence(place: ReminderDataItem) : Geofence {
    return Geofence.Builder()
            .setRequestId(place.id)
            .setCircularRegion(
                    place.latitude!!,
                    place.longitude!!,
                    150f
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(86400000) // 24 hours
            .build()
}

fun geofencesRequestCreator(geofencesList : List<Geofence>): GeofencingRequest? {
    return GeofencingRequest.Builder().apply {
        addGeofences(geofencesList)
        setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
    }.build()
}
