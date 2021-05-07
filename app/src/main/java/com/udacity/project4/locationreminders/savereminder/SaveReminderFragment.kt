package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.LocationUpdaterService
import com.udacity.project4.locationreminders.geofence.createGeofence
import com.udacity.project4.locationreminders.geofence.geofencesRequestCreator
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    private lateinit var geofencingClient : GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//             1) save the reminder to the local db
            val reminder = ReminderDataItem(title, description, location, latitude, longitude)

//            TODO: use the user entered reminder details to:
//             2) add a geofencing request [[DONE]]
            val permissionsToGrant = checkPermissions()
            if(permissionsToGrant.isEmpty() && _viewModel.validateEnteredData(reminder)) {
                val geofence = createGeofence(reminder)
                val geofencingRequest = geofencesRequestCreator(listOf(geofence))
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        _viewModel.validateAndSaveReminder(reminder)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            activity?.startService(Intent(activity, LocationUpdaterService::class.java))
                    }
                    addOnFailureListener { }
                }
            } else if(permissionsToGrant.isNotEmpty()){
                requestPermissions(permissionsToGrant)
            }
        }
    }

    /* Permissions */
    // Check permissions
    private fun checkPermissions() : List<String> {
        val permissionsNeeded = ArrayList<String>()

        if(!isFineLocationGranted())
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isBackgroundLocationGranted())
            permissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isForegroundServiceGranted())
            permissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE)

        return permissionsNeeded
    }

    private fun isFineLocationGranted() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED

    private fun isBackgroundLocationGranted() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION) === PackageManager.PERMISSION_GRANTED

    private fun isForegroundServiceGranted() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.FOREGROUND_SERVICE) === PackageManager.PERMISSION_GRANTED

   // Request permissions
   private fun requestPermissions(permissionsToGrant: List<String>){
       _viewModel.snackPermissionsRequest() //Message

       if(Manifest.permission.ACCESS_FINE_LOCATION in permissionsToGrant &&
               Manifest.permission.ACCESS_BACKGROUND_LOCATION in permissionsToGrant)
           requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                   PermissionsCodes.REQUEST_FINE_AND_BACKGROUND_LOCATION_PERMISSION)

       else if(Manifest.permission.ACCESS_FINE_LOCATION in permissionsToGrant)
           requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                   PermissionsCodes.REQUEST_FINE_LOCATION_PERMISSION)

       else if(Manifest.permission.ACCESS_BACKGROUND_LOCATION in permissionsToGrant)
           requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                   PermissionsCodes.REQUEST_BACKGROUND_LOCATION_PERMISSION)

       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Manifest.permission.FOREGROUND_SERVICE in permissionsToGrant)
           requestPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                   PermissionsCodes.REQUEST_FOREGROUND_SERVICE_PERMISSION)
   }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
