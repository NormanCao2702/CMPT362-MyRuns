package com.example.tranquangngoc_cao_myruns2

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // First create a temporary table with the new schema
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS exercise_entries_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    inputType TEXT NOT NULL,
                    activityType TEXT NOT NULL,
                    dateTime INTEGER NOT NULL,
                    duration REAL NOT NULL,
                    distance REAL NOT NULL,
                    calories INTEGER NOT NULL,
                    heartRate INTEGER NOT NULL,
                    comment TEXT NOT NULL,
                    avgSpeed REAL NOT NULL DEFAULT 0.0,
                    curSpeed REAL NOT NULL DEFAULT 0.0,
                    climb REAL NOT NULL DEFAULT 0.0,
                    locationList TEXT
                )
            """)

            // Copy the data from the old table
            database.execSQL("""
                INSERT INTO exercise_entries_new (
                    id, inputType, activityType, dateTime, duration, 
                    distance, calories, heartRate, comment
                )
                SELECT id, inputType, activityType, dateTime, duration, 
                       distance, calories, heartRate, comment
                FROM exercise_entries
            """)

            // Remove the old table
            database.execSQL("DROP TABLE exercise_entries")

            // Rename the new table to the correct name
            database.execSQL("ALTER TABLE exercise_entries_new RENAME TO exercise_entries")
        }
    }
}