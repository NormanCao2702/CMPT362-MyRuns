package com.example.tranquangngoc_cao_myruns2.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExerciseEntry::class], version = 2)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseEntryDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getDatabase(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                ).addMigrations(DatabaseMigrations.MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}