package com.example.tranquangngoc_cao_myruns2

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManualEntryActivity : AppCompatActivity() {

    private var selectedDateTime: Long? = null
    private lateinit var textViewDate: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewDuration: TextView
    private lateinit var textViewDistance: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewHeartRate: TextView
    private lateinit var textViewComment: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var repository: ExerciseRepository

    private var activityType: String = ""
    private var duration: Double = 0.0
    private var distance: Double = 0.0
    private var calories: Int = 0
    private var heartRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        activityType = intent.getStringExtra("activity_type") ?: "Dummy"

        val dao = ExerciseDatabase.getDatabase(this).exerciseDao()
        repository = ExerciseRepository(dao)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        textViewDate = findViewById(R.id.textViewDate)
        textViewTime = findViewById(R.id.textViewTime)
        textViewDuration = findViewById(R.id.textViewDuration)
        textViewDistance = findViewById(R.id.textViewDistance)
        textViewCalories = findViewById(R.id.textViewCalories)
        textViewHeartRate = findViewById(R.id.textViewHeartRate)
        textViewComment = findViewById(R.id.textViewComment)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
    }

    private fun setupClickListeners() {
        textViewDate.setOnClickListener { showDatePicker() }
        textViewTime.setOnClickListener { showTimePicker() }
        textViewDuration.setOnClickListener { showInputDialog("Duration") }
        textViewDistance.setOnClickListener { showInputDialog("Distance") }
        textViewCalories.setOnClickListener { showInputDialog("Calories") }
        textViewHeartRate.setOnClickListener { showInputDialog("Heart Rate") }
        textViewComment.setOnClickListener { showInputDialog("Comment") }

        buttonSave.setOnClickListener { saveEntry() }
        buttonCancel.setOnClickListener { finish() }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            val formattedDate = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(date)
            textViewDate.text = "Date: $formattedDate"
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            textViewTime.text = "Time: $formattedTime"
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun showInputDialog(field: String) {
        val input = EditText(this)
        input.inputType = when (field) {
            "Duration", "Calories", "Heart Rate" -> InputType.TYPE_CLASS_NUMBER
            "Distance" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            else -> InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(this)
            .setTitle(field)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                when (field) {
                    "Duration" -> {
                        textViewDuration.text = "Duration: $value"
                        duration = value.toDoubleOrNull() ?: 0.0
                    }
                    "Distance" -> {
                        textViewDistance.text = "Distance: $value"
                        distance = value.toDoubleOrNull() ?: 0.0
                    }
                    "Calories" -> {
                        textViewCalories.text = "Calories: $value"
                        calories = value.toIntOrNull() ?: 0
                    }
                    "Heart Rate" -> {
                        textViewHeartRate.text = "Heart Rate: $value"
                        heartRate = value.toIntOrNull() ?: 0
                    }
                    "Comment" -> textViewComment.text = "Comment: $value"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveEntry() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Get current timestamp if no date/time was selected
            val currentTimeMillis = System.currentTimeMillis()

            val entry = ExerciseEntry(
                inputType = "Manual Entry",
                activityType = activityType, // get from spinner
                dateTime = selectedDateTime ?: currentTimeMillis,
                duration = duration,
                distance = distance,
                calories = calories,
                heartRate = heartRate,
                comment = ""     
            )

            repository.insertEntry(entry)

            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }

}