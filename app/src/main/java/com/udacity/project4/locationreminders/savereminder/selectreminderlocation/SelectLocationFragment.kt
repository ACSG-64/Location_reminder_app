package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.PermissionsCodes
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var mapFragment : GoogleMap
    private lateinit var selectedMarker : Marker

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Kotlin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation [[DONE]]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as RemindersActivity)
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            onMapReady(it)
        }

//        TODO: call this function after the user confirms on the selected location [[DONE]]
        binding.saveSelectedLocationButton.setOnClickListener {
            onLocationSelected(selectedMarker)
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }

        return binding.root
    }

    private fun onLocationSelected(selectedPoi : Marker) {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence [[DONE]]
        _viewModel.apply {
            reminderSelectedLocationStr.value = selectedPoi.title
            latitude.value = selectedPoi.position.latitude
            longitude.value = selectedPoi.position.longitude
        }
    }

    /* Map */
    override fun onMapReady(map: GoogleMap?) {
        mapFragment = map!!

//        TODO: add style to the map [[DONE]]
        setMapStyle(mapFragment)

        getUserCurrentLocation()

        mapFragment.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            moveCamera(CameraUpdateFactory.zoomTo(0f))
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }

        setPoiClick(mapFragment)
        setCustomPosition(mapFragment)
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))

        } catch (e: Exception) {}
    }

//        TODO: put a marker to location that the user selected [[DONE]]
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear() // Clear all previous markers

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()

            getSelectedMarker(poiMarker)
        }
    }

    private fun setCustomPosition(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear() // Clear all previous markers

            val markerTitle = String.format(
                Locale.getDefault(),
                getString(R.string.free_marker_title),
                latLng.latitude,
                latLng.longitude
            )
            val marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(markerTitle)
            )
            marker.showInfoWindow()

            getSelectedMarker(marker)
        }
    }

    private fun getSelectedMarker(marker : Marker){
        selectedMarker = marker

        binding.saveSelectedLocationButton.isEnabled = true
        binding.saveSelectedLocationButton.isClickable = true
    }


    @SuppressLint("MissingPermission")
    private fun getUserCurrentLocation(){
        if(isPermissionGranted()){
            mapFragment.isMyLocationEnabled = true
            // Get the user current location and move camera to it
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null){
            //        TODO: zoom to the user location after taking his permission [[DONE]]
                    val currentPosition = LatLng(it.latitude, it.longitude)
                    mapFragment.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 18f))
                }
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PermissionsCodes.REQUEST_FINE_LOCATION_PERMISSION
            )
        }
    }

    /* Permissions */
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("EPIC PERMISSION", grantResults.toString())
        if(isPermissionGranted())
            getUserCurrentLocation()
    }

    /* Menu */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        TODO: Change the map type based on the user's selection. [[DONE]]
        R.id.normal_map -> {
            mapFragment.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mapFragment.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mapFragment.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mapFragment.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }



}
