package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LiveTrackingState(
    val isTrackingActive: Boolean = false,
    val bookingCode: String = "",
    val vehiclePlate: String = "",
    val driverName: String = "",
    val currentLatitude: Double = -6.2088,
    val currentLongitude: Double = 106.8456,
    val speedKmh: Float = 0f,
    val currentDestinationIndex: Int = 1,
    val totalDestinations: Int = 2,
    val geofenceAlert: Boolean = false,
    val geofenceMessage: String = ""
)

class TrackingForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var trackingJob: Job? = null

    companion object {
        const val CHANNEL_ID = "sipemo_live_tracking_channel"
        const val CHANNEL_NAME = "SiPemo Live GPS Tracking"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_TRACKING = "com.example.action.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.action.STOP_TRACKING"
        const val ACTION_TRIGGER_GEOFENCE_TEST = "com.example.action.TRIGGER_GEOFENCE_TEST"

        const val EXTRA_BOOKING_CODE = "extra_booking_code"
        const val EXTRA_VEHICLE_PLATE = "extra_vehicle_plate"
        const val EXTRA_DRIVER_NAME = "extra_driver_name"

        private val _trackingState = MutableStateFlow(LiveTrackingState())
        val trackingState: StateFlow<LiveTrackingState> = _trackingState.asStateFlow()

        fun startTracking(context: Context, bookingCode: String, vehiclePlate: String, driverName: String) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_BOOKING_CODE, bookingCode)
                putExtra(EXTRA_VEHICLE_PLATE, vehiclePlate)
                putExtra(EXTRA_DRIVER_NAME, driverName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }

        fun toggleGeofenceSimulation(deviated: Boolean) {
            val current = _trackingState.value
            _trackingState.value = current.copy(
                geofenceAlert = deviated,
                geofenceMessage = if (deviated) "PERINGATAN: Kendaraan terdeteksi menyimpang >1.2 km dari koridor rute tujuan!" else ""
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val bookingCode = intent.getStringExtra(EXTRA_BOOKING_CODE) ?: "SR-BOOKING"
                val vehiclePlate = intent.getStringExtra(EXTRA_VEHICLE_PLATE) ?: "B 1024 SR"
                val driverName = intent.getStringExtra(EXTRA_DRIVER_NAME) ?: "Staff Peminjam"

                startForegroundTracking(bookingCode, vehiclePlate, driverName)
            }
            ACTION_STOP_TRACKING -> {
                stopForegroundTracking()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi persisten pelacakan GPS armada mobil Sekolah Rakyat Indonesia"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundTracking(bookingCode: String, vehiclePlate: String, driverName: String) {
        val notification = buildTrackingNotification(
            title = "Pelacakan GPS Aktif • $vehiclePlate",
            contentText = "Peminjaman $bookingCode oleh $driverName sedang berjalan.",
            isAlert = false
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        _trackingState.value = LiveTrackingState(
            isTrackingActive = true,
            bookingCode = bookingCode,
            vehiclePlate = vehiclePlate,
            driverName = driverName,
            currentLatitude = -6.2088,
            currentLongitude = 106.8456,
            speedKmh = 38f,
            currentDestinationIndex = 1,
            totalDestinations = 2,
            geofenceAlert = false
        )

        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            // Coordinate movement simulation along waypoints
            val routeCoords = listOf(
                Pair(-6.2088, 106.8456), // Sekolah Rakyat base
                Pair(-6.2120, 106.8490),
                Pair(-6.2165, 106.8520),
                Pair(-6.2200, 106.8560), // Menuju Tujuan 1
                Pair(-6.2245, 106.8600),
                Pair(-6.2300, 106.8650)  // Menuju Tujuan 2
            )

            var step = 0
            while (isActive) {
                delay(3000) // update location every 3s
                step = (step + 1) % routeCoords.size
                val coord = routeCoords[step]
                val current = _trackingState.value

                val currentSpeed = if (step == 0) 0f else (32f + (step * 4) % 25)
                val targetIndex = if (step < 3) 1 else 2

                _trackingState.value = current.copy(
                    currentLatitude = coord.first,
                    currentLongitude = coord.second,
                    speedKmh = currentSpeed,
                    currentDestinationIndex = targetIndex
                )

                // Update notification text periodically
                updateNotification("Pelacakan GPS Aktif • $vehiclePlate", "Kecepatan: ${currentSpeed.toInt()} km/jam • Menuju Tujuan $targetIndex")
            }
        }
    }

    private fun updateNotification(title: String, contentText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notification = buildTrackingNotification(title, contentText, _trackingState.value.geofenceAlert)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildTrackingNotification(title: String, contentText: String, isAlert: Boolean): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isAlert) "⚠️ PERINGATAN GEOFENCE • $title" else title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(if (isAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setColor(if (isAlert) 0xFFD97706.toInt() else 0xFF059669.toInt())
            .build()
    }

    private fun stopForegroundTracking() {
        trackingJob?.cancel()
        _trackingState.value = LiveTrackingState(isTrackingActive = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
