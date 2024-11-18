package com.example.tranquangngoc_cao_myruns2.HistoryDetail

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseDatabase
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseEntry
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseRepository
import com.example.tranquangngoc_cao_myruns2.R
import com.example.tranquangngoc_cao_myruns2.Util.UnitConverter
import com.example.tranquangngoc_cao_myruns2.Util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DisplayEntryActivity : AppCompatActivity() {

    private lateinit var repository: ExerciseRepository
    private var entryId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_entry)

        // Get the entry ID passed from HistoryFragment
        entryId = intent.getLongExtra("entry_id", -1)

        // Initialize repository
        val database = ExerciseDatabase.getDatabase(this)
        repository = ExerciseRepository(database.exerciseDao())

        // Set up delete button
        findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            deleteEntry()
        }

        // Load and display entry
        loadEntry()
    }

    private fun loadEntry() {
        lifecycleScope.launch {
            val entry = withContext(Dispatchers.IO) {
                repository.getEntryById(entryId)
            }
            entry?.let { displayEntryDetails(it) }
        }
    }

    private fun displayEntryDetails(entry: ExerciseEntry) {
        // Get unit preference
        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", MODE_PRIVATE)
        val useMetric = sharedPreferences.getString("unit_preference", "metric") == "metric"

        // Display all fields
        findViewById<TextView>(R.id.textInputType).text = entry.inputType
        findViewById<TextView>(R.id.textActivityType).text = entry.activityType

        // Format date and time
        val dateFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.textDateTime).text = dateFormat.format(Date(entry.dateTime))

        // Format duration
        findViewById<TextView>(R.id.textDuration).text = Util.formatDuration(entry.duration)

        // Format distance based on unit preference
        val distance = if (useMetric) {
            UnitConverter.milesToKilometers(entry.distance)
        } else {
            entry.distance
        }
        val distanceUnit = if (useMetric) "Kilometers" else "Miles"
        val distanceFormatted = String.format("%.2f %s", distance, distanceUnit)
        findViewById<TextView>(R.id.textDistance).text = distanceFormatted

        // Display calories and heart rate
        findViewById<TextView>(R.id.textCalories).text = "${entry.calories} cals"
        findViewById<TextView>(R.id.textHeartRate).text = "${entry.heartRate} bpm"
    }

    private fun deleteEntry() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteEntry(entryId)
            }
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}