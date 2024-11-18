package com.example.tranquangngoc_cao_myruns2.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_entries")
data class ExerciseEntry (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inputType: String,    // Manual, GPS, or Automatic
    val activityType: String, // Running, Walking, etc.
    val dateTime: Long,       // Store as timestamp
    val duration: Double,     // in minutes
    val distance: Double,     // Store in miles (base unit), convert when displaying
    val calories: Int,
    val heartRate: Int,
    val comment: String,

    // New fields for GPS entries
    val avgSpeed: Double = 0.0,
    val curSpeed: Double = 0.0,
    val climb: Double = 0.0,
    val locationList: String? = null
)