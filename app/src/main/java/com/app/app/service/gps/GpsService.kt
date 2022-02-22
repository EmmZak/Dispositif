package com.app.app.service.gps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.util.Log
import androidx.core.app.ActivityCompat
import com.app.app.service.call.Contact
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

class GpsService {

    private val TAG = "GpsService manu"
    private var context :Context
    private var fusedLocationClient: FusedLocationProviderClient

    constructor(context: Context) {
        this.context = context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Return Google Maps url with the location
     */
    fun getLocation(): Task<Location>? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 99);
            Log.e(TAG, "gps no permission")
            return null
        }
        var mapUrl: String? = null
        val cancel = CancellationTokenSource()
        return fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, cancel.token)
/*            ?.addOnSuccessListener { loc : Location? ->
                Log.e(TAG, "location $loc")
                //val mapUrl = "https://www.google.com/maps/@${loc?.latitude},${loc?.longitude},20z"
                mapUrl = "https://www.google.com/maps/search/?api=1&query=${loc?.latitude},${loc?.longitude}"
                Log.e(TAG, "mapUrl $mapUrl")
            }
            ?.addOnFailureListener {
                Log.e(TAG, "error")
            }
        return mapUrl*/
    }
}

