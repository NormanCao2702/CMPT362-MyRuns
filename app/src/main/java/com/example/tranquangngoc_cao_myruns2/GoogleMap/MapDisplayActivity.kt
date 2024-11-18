package com.example.tranquangngoc_cao_myruns2.GoogleMap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseDatabase
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseEntry
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseRepository
import com.example.tranquangngoc_cao_myruns2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson


class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var polyline: Polyline? = null
    private var currentLocationMarker: Marker? = null
    private var startLocationMarker: Marker? = null
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // UI elements
    private lateinit var typeText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var curSpeedText: TextView
    private lateinit var climbText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    // ViewModel
    private val viewModel: MapViewModel by viewModels()

    // Settings
    private var isMetric: Boolean = false
    private var activityType: String = "Running"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_display)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map"

        // Initialize UI elements
        initializeViews()
        loadUnitPreference()

        // Get activity type from intent
        activityType = intent.getStringExtra("activity_type") ?: "Running"
        typeText.text = "Type: $activityType"

        // Setup map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup button listeners
        setupButtons()

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        val permissionsToRequest = mutableListOf<String>()

        // Add all required permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // For Android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // For Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // All permissions granted, start service
            startTrackingService()
            setupObservers()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // Check if at least location permissions are granted
                if (grantResults.isNotEmpty() &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                    startTrackingService()
                    setupObservers()
                } else {
                    // Show a more detailed explanation
                    AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("This app needs location permissions to track your exercise. Please grant location permissions in Settings.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            // Open app settings
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                                startActivity(this)
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            finish()
                        }
                        .show()
                }
            }
        }
    }


    private fun initializeViews() {
        typeText = findViewById(R.id.typeText)
        avgSpeedText = findViewById(R.id.avgSpeedText)
        curSpeedText = findViewById(R.id.curSpeedText)
        climbText = findViewById(R.id.climbText)
        calorieText = findViewById(R.id.calorieText)
        distanceText = findViewById(R.id.distanceText)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
    }

    private fun loadUnitPreference() {
        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        isMetric = sharedPreferences.getString("unit_preference", "imperial") == "metric"
    }

    private fun startTrackingService() {
        Intent(this, TrackingService::class.java).also { intent ->
            startService(intent)
            bindService(intent, viewModel, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setupObservers() {
        // Observe location updates
        viewModel.locationPoints.observe(this) { points ->
            Log.d("MapActivity", "Received location points: ${points.size}")
            updateMapTrace(points)
        }

        viewModel.isServiceBound.observe(this) { isBound ->
            Log.d("MapActivity", "Service bound: $isBound")
        }

        // Observe stats
        viewModel.distance.observe(this) { distance ->
            val displayDistance = if (isMetric) distance * 1.60934 else distance
            val unit = if (isMetric) "Kilometers" else "Miles"
            distanceText.text = "Distance: %.2f %s".format(displayDistance, unit)
        }

        viewModel.currentSpeed.observe(this) { speed ->
            val displaySpeed = if (isMetric) speed * 1.60934 else speed
            val unit = if (isMetric) "km/h" else "m/h"
            curSpeedText.text = "Cur speed: %.1f %s".format(displaySpeed, unit)
        }

        viewModel.averageSpeed.observe(this) { speed ->
            val displaySpeed = if (isMetric) speed * 1.60934 else speed
            val unit = if (isMetric) "km/h" else "m/h"
            avgSpeedText.text = "Avg speed: %.1f %s".format(displaySpeed, unit)
        }

        viewModel.calories.observe(this) { calories ->
            calorieText.text = "Calorie: ${calories.toInt()}"
        }

        viewModel.climb.observe(this) { climb ->
            val displayClimb = if (isMetric) climb * 1.60934 else climb
            val unit = if (isMetric) "Kilometers" else "Miles"
            climbText.text = "Climb: %.1f %s".format(displayClimb, unit)
        }
    }

    private fun updateMapTrace(points: List<LatLng>) {
        Log.d("MapActivity", "Updating map trace with ${points.size} points")
        if (points.isEmpty()) return


        // Update or create start marker
        if (startLocationMarker == null && points.isNotEmpty()) {
            startLocationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(points.first())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }

        // Update current location marker
        currentLocationMarker?.remove()
        currentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(points.last())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Update polyline
        polyline?.remove()
        polyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(Color.BLACK)
                .width(5f)
        )

        // Center map on current location if needed
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last(), 15f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun setupButtons() {
        buttonSave.setOnClickListener {
            saveExerciseEntry()
        }

        buttonCancel.setOnClickListener {
            stopTrackingAndFinish()
        }
    }

    private fun saveExerciseEntry() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Get final values from ViewModel
            val startTimeMillis = viewModel.startTime.value ?: System.currentTimeMillis()
            val durationInMinutes = (System.currentTimeMillis() - startTimeMillis) / (1000.0 * 60.0)
            val points = viewModel.locationPoints.value ?: emptyList()
            val distance = viewModel.distance.value ?: 0.0
            val calories = viewModel.calories.value ?: 0.0
            val avg_speed = viewModel.averageSpeed.value ?: 0.0

            val entry = ExerciseEntry(
                inputType = "GPS",
                activityType = activityType,
                dateTime = startTimeMillis,
                duration = durationInMinutes,  // Use the duration value
                distance = if (isMetric) distance * 1.60934 else distance,
                avgSpeed = avg_speed,
                calories = calories.toInt(),
                heartRate = 0,
                comment = "",
                locationList = Gson().toJson(points)
            )

            // Save using repository
            val repository = ExerciseRepository(
                ExerciseDatabase.getDatabase(this@MapDisplayActivity).exerciseDao()
            )
            repository.insertEntry(entry)

            withContext(Dispatchers.Main) {
                stopTrackingAndFinish()
            }
        }
    }

    private fun stopTrackingAndFinish() {
        unbindService(viewModel)
        stopService(Intent(this, TrackingService::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.isServiceBound.value == true) {
            unbindService(viewModel)
        }
    }
}
