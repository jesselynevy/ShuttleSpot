package com.beg.shuttlespot.data.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.beg.shuttlespot.R
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service yang mengumpulkan sampel GPS driver dan mengirimkannya ke
 * [DriverTripRepository.publishLocation]. Hanya jalan selama satu trip aktif.
 */
@AndroidEntryPoint
class ShuttleSpotLocationService : Service() {

    @Inject lateinit var driverTripRepository: DriverTripRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var currentTripId: String? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val tripId = currentTripId ?: return
            val loc = result.lastLocation ?: return
            scope.launch {
                driverTripRepository.publishLocation(
                    tripId = tripId,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
                    capturedAtEpochMillis = loc.time,
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tripId = intent?.getStringExtra(EXTRA_TRIP_ID)
        if (tripId.isNullOrBlank()) {
            Log.w(TAG, "onStartCommand without tripId; stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        currentTripId = tripId

        startAsForeground()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startAsForeground() {
        ensureNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.shuttle_spot_icon)
            .setContentTitle(getString(R.string.location_service_title))
            .setContentText(getString(R.string.location_service_message))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (security: SecurityException) {
            Log.e(TAG, "Missing location permission; stopping service", security)
            stopSelf()
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.location_service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.location_service_channel_description)
                setShowBadge(false)
            }
        )
    }

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LocationService"
        private const val CHANNEL_ID = "shuttlespot.location"
        private const val NOTIFICATION_ID = 42
        private const val EXTRA_TRIP_ID = "extra.tripId"

        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val MIN_UPDATE_INTERVAL_MS = 2_000L

        fun start(context: Context, tripId: String) {
            val intent = Intent(context, ShuttleSpotLocationService::class.java)
                .putExtra(EXTRA_TRIP_ID, tripId)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ShuttleSpotLocationService::class.java))
        }
    }
}