package com.example.tranquangngoc_cao_myruns2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapDetailActivity: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var repository: ExerciseRepository
    private var entryId: Long = -1
    private var isMetric: Boolean = false

    // UI elements
    private lateinit var typeText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var curSpeedText: TextView
    private lateinit var climbText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView
    private lateinit var buttonDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_detail)

        // Get entry ID from intent
        entryId = intent.getLongExtra("entry_id", -1)
        if (entryId == -1L) {
            finish()
            return
        }

        // Initialize repository
        val database = ExerciseDatabase.getDatabase(this)
        repository = ExerciseRepository(database.exerciseDao())

        // Initialize UI elements
        initializeViews()

        // Load unit preference
        loadUnitPreference()

        // Setup map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup delete button
        setupDeleteButton()
    }

    private fun initializeViews() {
        typeText = findViewById(R.id.typeText)
        avgSpeedText = findViewById(R.id.avgSpeedText)
        curSpeedText = findViewById(R.id.curSpeedText)
        climbText = findViewById(R.id.climbText)
        calorieText = findViewById(R.id.calorieText)
        distanceText = findViewById(R.id.distanceText)
        buttonDelete = findViewById(R.id.buttonDelete)
    }

    private fun loadUnitPreference() {
        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        isMetric = sharedPreferences.getString("unit_preference", "metric") == "metric"
    }

    private fun setupDeleteButton() {
        buttonDelete.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    repository.deleteEntry(entryId)
                }
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        loadExerciseEntry()
    }

    private fun loadExerciseEntry() {
        lifecycleScope.launch {
            val entry = withContext(Dispatchers.IO) {
                repository.getEntryById(entryId)
            }

            entry?.let { displayEntry(it) }
        }
    }

    private fun displayEntry(entry: ExerciseEntry) {
        // Display basic info
        typeText.text = "Type: ${entry.activityType}"

        // Display speeds with unit conversion
        val speedUnit = if (isMetric) "km/h" else "m/h"
        val avgSpeed = if (isMetric) entry.avgSpeed * 1.60934 else entry.avgSpeed
        val curSpeed = if (isMetric) entry.curSpeed * 1.60934 else entry.curSpeed

        avgSpeedText.text = "Avg speed: %.1f %s".format(avgSpeed, speedUnit)
        curSpeedText.text = "Cur speed: %.1f %s".format(curSpeed, speedUnit)

        // Display climb
        climbText.text = "Climb: ${entry.climb}"

        // Display calories
        calorieText.text = "Calorie: ${entry.calories}"

        // Display distance with unit conversion
        val distance = if (isMetric) {
            UnitConverter.milesToKilometers(entry.distance)
        } else {
            entry.distance
        }
        val distanceUnit = if (isMetric) "Kilometers" else "Miles"
        distanceText.text = "Distance: %.2f %s".format(distance, distanceUnit)

        // Draw route on map
        drawRouteOnMap(entry.locationList)
    }

    private fun drawRouteOnMap(locationListJson: String?) {
        if (locationListJson == null) return

        try {
            val gson = Gson()
            val type = object : TypeToken<List<LatLng>>() {}.type
            val points: List<LatLng> = gson.fromJson(locationListJson, type)

            if (points.isNotEmpty()) {
                // Draw polyline
                mMap.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .color(Color.BLACK)
                        .width(5f)
                )

                // Add a green marker for the start
                mMap.addMarker(
                    MarkerOptions()
                        .position(points.first())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )

                // Add a red marker for the end, only if there are multiple points
                if (points.size > 1) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(points.last())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                }


                // Move camera to show the route
                val bounds = LatLngBounds.builder()
                points.forEach { bounds.include(it) }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle JSON parsing error
        }
    }
}