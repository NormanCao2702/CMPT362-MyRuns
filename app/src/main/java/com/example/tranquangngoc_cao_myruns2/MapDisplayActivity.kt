package com.example.tranquangngoc_cao_myruns2

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import androidx.core.app.ActivityCompat
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

class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private var markers: MutableList<Marker> = mutableListOf()
    private var polyline: Polyline? = null
    private val points = mutableListOf<LatLng>()
    private var startTime: Long = 0L  // For time tracking
    private var lastMarkerTime: Long = 0L  // For current speed
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())  // Coroutine scope for this activity

    // UI elements
    private lateinit var typeText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var curSpeedText: TextView
    private lateinit var climbText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private var isMetric: Boolean = false

    // Activity type from intent
    private var activityType: String = "Running" // default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_display)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Initialize UI elements
        initializeViews()

        // Load unit preference
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
    }

    private fun loadUnitPreference() {
        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        val unitPref = sharedPreferences.getString("unit_preference", "imperial") // default to imperial
        isMetric = unitPref == "metric"
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

    private fun setupButtons() {
        buttonSave.setOnClickListener {
            // TODO: Save functionality will be implemented later
            finish()
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Initialize map settings
        // Set long click listener
        // Set initial location (Google campus) and add initial marker
        mMap = googleMap
        startTime = System.currentTimeMillis()
        lastMarkerTime = startTime

        // Set up map settings
        mMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapLongClickListener(this@MapDisplayActivity)
        }

        // Set initial location (Google campus) and add initial marker
        val initialLocation = LatLng(37.4219999, -122.0862462)
        val initialMarker = mMap.addMarker(
            MarkerOptions()
                .position(initialLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Add initial marker to our lists
        initialMarker?.let {
            markers.add(it)
            points.add(initialLocation)
        }

        // Move camera to initial location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))

        // Initialize stats with 0 distance
        updateStats()
    }

    override fun onMapLongClick(latLng: LatLng) {
        // Add marker
        // Update polyline
        // Update distance and other stats
        // Add marker at long-pressed location
        lastMarkerTime = System.currentTimeMillis()

        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        marker?.let {
            markers.add(it)
            points.add(latLng)
            updatePolyline()
            updateStats()
        }
    }

    private fun updatePolyline() {
        // Remove existing polyline
        polyline?.remove()

        // Draw new polyline if we have at least 2 points
        if (points.size >= 2) {
            polyline = mMap.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .color(android.graphics.Color.BLACK)
                    .width(5f)
            )
        }
    }

    private fun updateStats() {
        activityScope.launch {
            // Calculate total distance between all points
            val totalDistance = calculateTotalDistance()

            // Convert meters to miles/kilometers based on preference
            val distanceInMiles = totalDistance * 0.000621371
            val displayDistance = if (isMetric) UnitConverter.milesToKilometers(distanceInMiles) else distanceInMiles

            val currentSpeed = calculateCurrentSpeed()
            val averageSpeed = calculateAverageSpeed(distanceInMiles)

            val calories = calculateCalories(distanceInMiles)

            updateUI(displayDistance, currentSpeed, averageSpeed, calories)
        }
    }


    // ----------- Helpers --------------
    private suspend fun calculateTotalDistance(): Float = withContext(Dispatchers.Default) {
        var totalDistance = 0.0f
        for (i in 0 until points.size - 1) {
            val results = FloatArray(1)
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            totalDistance += results[0]
        }
        totalDistance
    }

    private fun updateUI(displayDistance: Double, currentSpeed: Double, averageSpeed: Double, calories: Int) {
        val distanceUnit = if (isMetric) "Kilometers" else "Miles"
        val speedUnit = if (isMetric) "km/h" else "m/h"

        distanceText.text = "Distance: %.2f %s".format(displayDistance, distanceUnit)
        curSpeedText.text = "Cur speed: %.1f %s".format(currentSpeed, speedUnit)
        avgSpeedText.text = "Avg speed: %.1f %s".format(averageSpeed, speedUnit)
        climbText.text = "Climb: 0"
        calorieText.text = "Calorie: $calories"
    }

    private fun calculateCurrentSpeed(): Double {
        if (points.size < 2) return 0.0

        // Get last two points
        val lastPoint = points[points.size - 1]
        val previousPoint = points[points.size - 2]

        // Calculate distance between last two points
        val results = FloatArray(1)
        Location.distanceBetween(
            previousPoint.latitude, previousPoint.longitude,
            lastPoint.latitude, lastPoint.longitude,
            results
        )

        val distance = results[0] * 0.000621371  // Convert to miles
        val timeInHours = (lastMarkerTime - startTime) / (1000.0 * 60.0 * 60.0)  // Convert to hours
        return if (timeInHours > 0) distance / timeInHours else 0.0
    }

    private fun calculateAverageSpeed(totalDistanceMiles: Double): Double {
        val totalTimeInHours = (System.currentTimeMillis() - startTime) / (1000.0 * 60.0 * 60.0)
        return if (totalTimeInHours > 0) totalDistanceMiles / totalTimeInHours else 0.0
    }

    private fun calculateCalories(distanceMiles: Double): Int {
        val weight = 70.0  // Default weight in kg if not set
        val coefficient = when (activityType) {
            "Running" -> 0.75
            "Walking" -> 0.53
            else -> 0.65  // Average for other activities
        }
        return (distanceMiles * weight * coefficient).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()  // Cancel coroutines when the activity is destroyed
    }

}

// Unused for updateStats function
//        // Calculate total distance between all points
//        var totalDistance = 0.0f
//
//        for (i in 0 until points.size - 1) {
//            val results = FloatArray(1)
//            Location.distanceBetween(
//                points[i].latitude, points[i].longitude,
//                points[i + 1].latitude, points[i + 1].longitude,
//                results
//            )
//            totalDistance += results[0]
//        }
//
//        // Convert meters to miles/kilometers based on preference
//        val distanceInMiles = totalDistance * 0.000621371
//        val displayDistance = if (isMetric) {
//            UnitConverter.milesToKilometers(distanceInMiles)  // Use the function
//        } else {
//            distanceInMiles
//        }
//
//        // Calculate speeds
//        val currentSpeed = calculateCurrentSpeed()
//        val averageSpeed = calculateAverageSpeed(distanceInMiles)
//
//        // Convert speeds if metric
//        val displayCurrentSpeed = if (isMetric) currentSpeed * 1.60934 else currentSpeed
//        val displayAverageSpeed = if (isMetric) averageSpeed * 1.60934 else averageSpeed
//
//        // Calculate calories
//        val calories = calculateCalories(distanceInMiles)
//
//        // Update UI with appropriate units
//        val distanceUnit = if (isMetric) "Kilometers" else "Miles"
//        val speedUnit = if (isMetric) "km/h" else "m/h"
//
//        distanceText.text = "Distance: %.2f %s".format(displayDistance, distanceUnit)
//        curSpeedText.text = "Cur speed: %.1f %s".format(displayCurrentSpeed, speedUnit)
//        avgSpeedText.text = "Avg speed: %.1f %s".format(displayAverageSpeed, speedUnit)
//        climbText.text = "Climb: 0"
//        calorieText.text = "Calorie: $calories"