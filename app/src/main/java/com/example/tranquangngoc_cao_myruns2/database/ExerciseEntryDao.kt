package com.example.tranquangngoc_cao_myruns2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseEntryDao {
    @Insert
    suspend fun insert(entry: ExerciseEntry): Long

    @Query("SELECT * FROM exercise_entries ORDER BY dateTime ASC")
    suspend fun getAllEntries(): List<ExerciseEntry>

    @Delete
    suspend fun deleteEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): ExerciseEntry?
}