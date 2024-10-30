package com.example.tranquangngoc_cao_myruns2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ExerciseRepository(private val exerciseDao: ExerciseEntryDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun insertEntry(entry: ExerciseEntry): Long {
        return exerciseDao.insert(entry)
    }

    suspend fun getAllEntries(): List<ExerciseEntry> {
        return exerciseDao.getAllEntries()
    }

    suspend fun deleteEntry(entryId: Long) {
        val entry = getEntryById(entryId)
        if (entry != null) {
            exerciseDao.deleteEntry(entry)
        }
    }

    suspend fun getEntryById(id: Long): ExerciseEntry? {
        return exerciseDao.getEntryById(id)
    }
}