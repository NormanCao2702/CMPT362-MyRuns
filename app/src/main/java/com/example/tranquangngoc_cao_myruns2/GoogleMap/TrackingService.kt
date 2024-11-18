package com.example.tranquangngoc_cao_myruns2.GoogleMap

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tranquangngoc_cao_myruns2.R
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class TrackingService : Service() {
    // About notification
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "tracking_channel"
    private val NOTIFICATION_ID = 1
    private var isFirstLocation = true

    // About location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationPoints = mutableListOf<LatLng>()
    private var lastLocation: Location? = null

    // About stats
    private var duration: Double = 0.0
    private var distance: Double = 0.0
    private var curSpeed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var calories: Double = 0.0
    private var climb: Double = 0.0

    private var startTime: Long = 0L
    private var lastTime: Long = 0L

    // About binding
    private val binder = MyBinder()
    private var msgHandler: Handler? = null

    companion object {
        const val MSG_INT_VALUE = 0
    }

    override fun onCreate() {
        super.onCreate()
        showNotification()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        startLocationUpdates()

        startTime = System.currentTimeMillis()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("TrackingService", "Received location update")
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    if (isFirstLocation) {
                        // First location received, start tracking from here
                        isFirstLocation = false
                        startTime = System.currentTimeMillis()
                    }
                    Log.d("TrackingService", "New location: $latLng")
                    locationPoints.add(latLng)

                    lastLocation?.let { last ->
                        val currentTime = System.currentTimeMillis()
                        duration = ((currentTime - startTime) / 1000).toDouble()

                        // Update distance (convert to miles)
                        distance += last.distanceTo(location) / 1609.344f

                        // Calculate speeds
                        curSpeed = ((last.distanceTo(location) / 1609.344f) /
                                ((currentTime - lastTime) / 3600000.0))
                        avgSpeed = distance / (duration / 3600)

                        // Simple calorie calculation
                        calories = distance * 120

                        // Calculate climb
                        climb = (location.altitude - last.altitude) / 1000
                    }

                    lastLocation = location
                    lastTime = System.currentTimeMillis()
//                    calculateStats(location)
                    sendMessage()
                }
            }
        }
    }

    private fun showNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon
            .setContentTitle("MyRuns")
            .setContentText("Recording your path now")

        // Set notification click action
        val intent = Intent(this, MapDisplayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.setContentIntent(pendingIntent)

        // Create notification channel for Android 8+
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Exercise Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
    }


    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(1000L) // 1 second interval
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(0f)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            Log.e("TrackingService", "Location permission not granted", e)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(handler: Handler) {
            this@TrackingService.msgHandler = handler
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        return true
    }

    private fun sendMessage() {
        try {
            msgHandler?.let { handler ->
                val bundle = Bundle().apply {
                    Log.d("TrackingService", "Sending message with ${locationPoints.size} points")
                    putDouble("DURATION", duration)
                    putDouble("DISTANCE", distance)
                    putDouble("CURRENT_SPEED", curSpeed)
                    putDouble("AVERAGE_SPEED", avgSpeed)
                    putDouble("CALORIES", calories)
                    putDouble("CLIMB", climb)
                    putString("LOCATION", Gson().toJson(locationPoints))
                    putLong("START_TIME", startTime)
                }

                val message = handler.obtainMessage().apply {
                    data = bundle
                    what = MSG_INT_VALUE
                }
                handler.sendMessage(message)
            }
        } catch (t: Throwable) {
            Log.e("TrackingService", "Failed to send message", t)
        }
    }

    private fun cleanup() {
        msgHandler = null
        notificationManager.cancel(NOTIFICATION_ID)
        stopLocationUpdates()
        locationPoints.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
}