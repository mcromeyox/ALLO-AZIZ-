package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val addressLabel: String = ""
)

object LocationServiceManager {

    // Default reference coordinate (Casablanca / Mohammedia hub)
    const val DEFAULT_LAT = 33.6835
    const val DEFAULT_LNG = -7.3849

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): DeviceLocation? {
        if (!hasLocationPermission(context)) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val addressLabel = reverseGeocodeCoordinates(context, location.latitude, location.longitude)
                    continuation.resume(
                        DeviceLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            speed = location.speed,
                            bearing = location.bearing,
                            altitude = location.altitude,
                            timestamp = location.time,
                            addressLabel = addressLabel
                        )
                    )
                } else {
                    // Try last known location as fallback
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            val addressLabel = reverseGeocodeCoordinates(context, lastLoc.latitude, lastLoc.longitude)
                            continuation.resume(
                                DeviceLocation(
                                    latitude = lastLoc.latitude,
                                    longitude = lastLoc.longitude,
                                    accuracy = lastLoc.accuracy,
                                    speed = lastLoc.speed,
                                    bearing = lastLoc.bearing,
                                    altitude = lastLoc.altitude,
                                    timestamp = lastLoc.time,
                                    addressLabel = addressLabel
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(context: Context, intervalMillis: Long = 4000L): Flow<DeviceLocation> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close()
            return@callbackFlow
        }

        val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setMinUpdateDistanceMeters(2f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val addressLabel = reverseGeocodeCoordinates(context, loc.latitude, loc.longitude)
                    trySend(
                        DeviceLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracy = loc.accuracy,
                            speed = loc.speed,
                            bearing = loc.bearing,
                            altitude = loc.altitude,
                            timestamp = loc.time,
                            addressLabel = addressLabel
                        )
                    )
                }
            }
        }

        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Calculate direct distance in Kilometers between two coordinates using android.location.Location
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return (results[0] / 1000.0)
    }

    /**
     * Estimate delivery time in minutes based on real distance in km (assuming city delivery speed ~25 km/h + 5 min buffer)
     */
    fun estimateMinutesLeft(distanceKm: Double): Int {
        val travelHours = distanceKm / 25.0
        val mins = (travelHours * 60).toInt() + 5
        return mins.coerceAtLeast(3)
    }

    /**
     * Reverse geocode location to city/street name for Moroccan cities
     */
    fun reverseGeocodeCoordinates(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("ar", "MA"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Return synchronous fallback or basic label
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: "المحمدية"
                    "$feature - ${addr.adminArea ?: "جهة الدار البيضاء - سطات"}"
                } else {
                    formatCoordsFallback(lat, lng)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: "المحمدية"
                    "$feature - ${addr.adminArea ?: "جهة الدار البيضاء - سطات"}"
                } else {
                    formatCoordsFallback(lat, lng)
                }
            }
        } catch (_: Exception) {
            formatCoordsFallback(lat, lng)
        }
    }

    private fun formatCoordsFallback(lat: Double, lng: Double): String {
        return "الموقع: %.4f, %.4f".format(Locale.US, lat, lng)
    }

    /**
     * Launch Google Maps app or web browser for turn-by-turn navigation or route overview
     */
    fun openGoogleMapsNavigation(context: Context, destinationLat: Double, destinationLng: Double, destinationLabel: String = "العميل") {
        try {
            // Try Google Maps Navigation URI scheme
            val navUri = Uri.parse("google.navigation:q=$destinationLat,$destinationLng&mode=d")
            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
                return
            }
        } catch (_: Exception) {}

        // Fallback to generic geo URI
        try {
            val geoUri = Uri.parse("geo:$destinationLat,$destinationLng?q=$destinationLat,$destinationLng($destinationLabel)")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            // Final fallback to Google Maps web directions URL
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
