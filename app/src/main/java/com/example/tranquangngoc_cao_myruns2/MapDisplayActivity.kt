package com.example.tranquangngoc_cao_myruns2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
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
import com.example.tranquangngoc_cao_myruns2.UnitConverter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
        when {
            // Check if we have all permissions
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // We have permissions, start service
                startTrackingService()
                setupObservers()
            }

            // Should we show rationale?
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs location permissions to track your exercise route.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissions()
                    }
                    .create()
                    .show()
            }

            else -> {
                // No explanation needed, request the permissions
                requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        try {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("MapActivity", "Error requesting permissions", e)
            Toast.makeText(
                this,
                "Error requesting permissions. Please grant permissions in settings.",
                Toast.LENGTH_LONG
            ).show()
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
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted
                    startTrackingService()
                    setupObservers()
                } else {
                    // Permission denied
                    Toast.makeText(
                        this,
                        "Location permissions are required for tracking",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
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
            val points = viewModel.locationPoints.value ?: emptyList()
            val distance = viewModel.distance.value ?: 0.0
            val calories = viewModel.calories.value ?: 0.0
            val duration = viewModel.duration.value ?: 0.0
            val avg_speed = viewModel.averageSpeed.value ?: 0.0

            val entry = ExerciseEntry(
                inputType = "GPS",
                activityType = activityType,
                dateTime = System.currentTimeMillis(),
                duration = duration,  // Use the duration value
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

//class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
//
//    private lateinit var mMap: GoogleMap
//    private var markers: MutableList<Marker> = mutableListOf()
//    private var polyline: Polyline? = null
//    private val points = mutableListOf<LatLng>()
//    private var startTime: Long = 0L  // For time tracking
//    private var lastMarkerTime: Long = 0L  // For current speed
//    private val activityScope = CoroutineScope(Dispatchers.Main + Job())  // Coroutine scope for this activity
//
//    // added new
//    private lateinit var locationCallback: LocationCallback
//    private var lastLocation: Location? = null
//    private var lastUpdateTime: Long = 0L
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private val speedReadings = mutableListOf<Double>()
//    //
//
//    // UI elements
//    private lateinit var typeText: TextView
//    private lateinit var avgSpeedText: TextView
//    private lateinit var curSpeedText: TextView
//    private lateinit var climbText: TextView
//    private lateinit var calorieText: TextView
//    private lateinit var distanceText: TextView
//    private lateinit var buttonSave: Button
//    private lateinit var buttonCancel: Button
//    private var isMetric: Boolean = false
//
//    // Activity type from intent
//    private var activityType: String = "Running" // default value
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_map_display)
//
//        // Setup toolbar
//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//
//        // Initialize UI elements
//        initializeViews()
//
//        // Load unit preference
//        loadUnitPreference()
//
//        // Get activity type from intent
//        activityType = intent.getStringExtra("activity_type") ?: "Running"
//        typeText.text = "Type: $activityType"
//
//        // Setup map
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        // Setup button listeners
//        setupButtons()
//
//        // added new
////        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//    }
//
//
//    private fun loadUnitPreference() {
//        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
//        val unitPref = sharedPreferences.getString("unit_preference", "imperial") // default to imperial
//        isMetric = unitPref == "metric"
//    }
//
//    private fun initializeViews() {
//        typeText = findViewById(R.id.typeText)
//        avgSpeedText = findViewById(R.id.avgSpeedText)
//        curSpeedText = findViewById(R.id.curSpeedText)
//        climbText = findViewById(R.id.climbText)
//        calorieText = findViewById(R.id.calorieText)
//        distanceText = findViewById(R.id.distanceText)
//        buttonSave = findViewById(R.id.buttonSave)
//        buttonCancel = findViewById(R.id.buttonCancel)
//    }
//
//    private fun setupButtons() {
//        buttonSave.setOnClickListener {
//            // TODO: Save functionality will be implemented later
//            saveExerciseEntry()
//        }
//
//        buttonCancel.setOnClickListener {
//            finish()
//        }
//    }
//
//    private fun saveExerciseEntry() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val duration = (System.currentTimeMillis() - startTime) / (1000.0 * 60.0) // Convert to minutes
//
//            // Get total distance in miles (since we store in miles)
//            val totalDistance = calculateTotalDistance() * 0.000621371 // Convert meters to miles
//
//            // Convert points list to JSON string
//            val gson = Gson()
//            val pointsJson = gson.toJson(points)
//
//            val entry = ExerciseEntry(
//                inputType = intent.getStringExtra("input_type") ?: "GPS",
//                activityType = activityType,
//                dateTime = startTime,
//                duration = duration,
//                distance = totalDistance,
//                calories = calculateCalories(totalDistance),
//                heartRate = 0,  // Not applicable for GPS tracking
//                comment = "",   // Not applicable for GPS tracking
//                avgSpeed = calculateAverageSpeed(totalDistance),
//                curSpeed = calculateCurrentSpeed(),
//                climb = 0.0,  // You could implement climb calculation if needed
//                locationList = pointsJson
//            )
//
//            // Save to database using your repository
//            val repository = ExerciseRepository(ExerciseDatabase.getDatabase(this@MapDisplayActivity).exerciseDao())
//            repository.insertEntry(entry)
//
//            withContext(Dispatchers.Main) {
//                finish()
//            }
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        // Initialize map settings
//        // Set long click listener
//        // Set initial location (Google campus) and add initial marker
//        mMap = googleMap
//        startTime = System.currentTimeMillis()
//        lastMarkerTime = startTime
//
//        // Set up map settings
//        mMap.apply {
//            uiSettings.isZoomControlsEnabled = true
//            mapType = GoogleMap.MAP_TYPE_NORMAL
//            setOnMapLongClickListener(this@MapDisplayActivity)
//        }
//
//        // Set initial location (Google campus) and add initial marker
//        val initialLocation = LatLng(37.4219999, -122.0862462)
//        val initialMarker = mMap.addMarker(
//            MarkerOptions()
//                .position(initialLocation)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        )
//
//        // Add initial marker to our lists
//        initialMarker?.let {
//            markers.add(it)
//            points.add(initialLocation)
//        }
//
//        // Move camera to initial location
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))
//
//        // Initialize stats with 0 distance
//        updateStats()
//    }
//
//    override fun onMapLongClick(latLng: LatLng) {
//        // Add marker
//        // Update polyline
//        // Update distance and other stats
//        // Add marker at long-pressed location
//        lastMarkerTime = System.currentTimeMillis()
//
//        val marker = mMap.addMarker(
//            MarkerOptions()
//                .position(latLng)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        )
//
//        marker?.let {
//            markers.add(it)
//            points.add(latLng)
//            updatePolyline()
//            updateStats()
//        }
//    }
//
//    private fun updatePolyline() {
//        // Remove existing polyline
//        polyline?.remove()
//
//        // Draw new polyline if we have at least 2 points
//        if (points.size >= 2) {
//            polyline = mMap.addPolyline(
//                PolylineOptions()
//                    .addAll(points)
//                    .color(android.graphics.Color.BLACK)
//                    .width(5f)
//            )
//        }
//    }
//
//    private fun updateStats() {
//        activityScope.launch {
//            // Calculate total distance between all points
//            val totalDistance = calculateTotalDistance()
//
//            // Convert meters to miles/kilometers based on preference
//            val distanceInMiles = totalDistance * 0.000621371
//            val displayDistance = if (isMetric) UnitConverter.milesToKilometers(distanceInMiles) else distanceInMiles
//
//            val currentSpeed = calculateCurrentSpeed()
//            val averageSpeed = calculateAverageSpeed(distanceInMiles)
//
//            val calories = calculateCalories(distanceInMiles)
//
//            updateUI(displayDistance, currentSpeed, averageSpeed, calories)
//        }
//    }
//
//
//    // ----------- Helpers --------------
//    private suspend fun calculateTotalDistance(): Float = withContext(Dispatchers.Default) {
//        var totalDistance = 0.0f
//        for (i in 0 until points.size - 1) {
//            val results = FloatArray(1)
//            Location.distanceBetween(
//                points[i].latitude, points[i].longitude,
//                points[i + 1].latitude, points[i + 1].longitude,
//                results
//            )
//            totalDistance += results[0]
//        }
//        totalDistance
//    }
//
//    private fun updateUI(displayDistance: Double, currentSpeed: Double, averageSpeed: Double, calories: Int) {
//        val distanceUnit = if (isMetric) "Kilometers" else "Miles"
//        val speedUnit = if (isMetric) "km/h" else "m/h"
//
//        distanceText.text = "Distance: %.2f %s".format(displayDistance, distanceUnit)
//        curSpeedText.text = "Cur speed: %.1f %s".format(currentSpeed, speedUnit)
//        avgSpeedText.text = "Avg speed: %.1f %s".format(averageSpeed, speedUnit)
//        climbText.text = "Climb: 0"
//        calorieText.text = "Calorie: $calories"
//    }
//
//    private fun calculateCurrentSpeed(): Double {
//        if (points.size < 2) return 0.0
//
//        // Get last two points
//        val lastPoint = points[points.size - 1]
//        val previousPoint = points[points.size - 2]
//
//        // Calculate distance between last two points
//        val results = FloatArray(1)
//        Location.distanceBetween(
//            previousPoint.latitude, previousPoint.longitude,
//            lastPoint.latitude, lastPoint.longitude,
//            results
//        )
//
//        val distance = results[0] * 0.000621371  // Convert to miles
//        val timeInHours = (lastMarkerTime - startTime) / (1000.0 * 60.0 * 60.0)  // Convert to hours
//        return if (timeInHours > 0) distance / timeInHours else 0.0
//    }
//
//    private fun calculateAverageSpeed(totalDistanceMiles: Double): Double {
//        val totalTimeInHours = (System.currentTimeMillis() - startTime) / (1000.0 * 60.0 * 60.0)
//        return if (totalTimeInHours > 0) totalDistanceMiles / totalTimeInHours else 0.0
//    }
//
//    private fun calculateCalories(distanceMiles: Double): Int {
//        val weight = 70.0  // Default weight in kg if not set
//        val coefficient = when (activityType) {
//            "Running" -> 0.75
//            "Walking" -> 0.53
//            else -> 0.65  // Average for other activities
//        }
//        return (distanceMiles * weight * coefficient).toInt()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        activityScope.cancel()  // Cancel coroutines when the activity is destroyed
//    }
//
//}