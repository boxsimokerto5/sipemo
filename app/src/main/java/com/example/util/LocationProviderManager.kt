package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserGpsCoordinate(
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val accuracyMeters: Float = 12f,
    val isRealGps: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

object LocationProviderManager {

    private val _currentCoordinate = MutableStateFlow(
        UserGpsCoordinate(
            latitude = -6.2088,
            longitude = 106.8456,
            accuracyMeters = 10f,
            isRealGps = false
        )
    )
    val currentCoordinate: StateFlow<UserGpsCoordinate> = _currentCoordinate.asStateFlow()

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var isListening = false

    fun initialize(context: Context) {
        if (fusedClient == null) {
            fusedClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        }
        startUpdates(context)
    }

    @SuppressLint("MissingPermission")
    fun startUpdates(context: Context) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return
        if (fusedClient == null) {
            fusedClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        }

        try {
            // First attempt to grab last known location immediately
            fusedClient?.lastLocation?.addOnSuccessListener { loc ->
                if (loc != null) {
                    _currentCoordinate.value = UserGpsCoordinate(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        isRealGps = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }

            // Register continuous location updates
            if (!isListening) {
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val loc = result.lastLocation ?: return
                        _currentCoordinate.value = UserGpsCoordinate(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            isRealGps = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                }

                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    4000L
                ).apply {
                    setMinUpdateIntervalMillis(2000L)
                    setMinUpdateDistanceMeters(2f)
                }.build()

                locationCallback?.let { cb ->
                    fusedClient?.requestLocationUpdates(locationRequest, cb, Looper.getMainLooper())
                    isListening = true
                }
            }
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }

    fun stopUpdates() {
        locationCallback?.let { cb ->
            fusedClient?.removeLocationUpdates(cb)
            isListening = false
        }
    }
}
