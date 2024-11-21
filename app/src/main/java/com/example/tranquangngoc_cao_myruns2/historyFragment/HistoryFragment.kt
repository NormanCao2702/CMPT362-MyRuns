package com.example.tranquangngoc_cao_myruns2.historyFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tranquangngoc_cao_myruns2.database.ExerciseDatabase
import com.example.tranquangngoc_cao_myruns2.database.ExerciseRepository
import com.example.tranquangngoc_cao_myruns2.historyDetail.DisplayEntryActivity
import com.example.tranquangngoc_cao_myruns2.historyDetail.MapDetailActivity
import com.example.tranquangngoc_cao_myruns2.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment: Fragment()  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var repository: ExerciseRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize repository
        val database = ExerciseDatabase.getDatabase(requireContext())
        repository = ExerciseRepository(database.exerciseDao())

        // Get unit preference
        val sharedPreferences = requireContext().getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        val useMetric = sharedPreferences.getString("unit_preference", "metric") == "metric"

        // Initialize adapter
        adapter = HistoryAdapter(emptyList(), useMetric) { entry ->
            // Launch appropriate activity based on input type
            val intent = when (entry.inputType) {
                "GPS" -> Intent(requireContext(), MapDetailActivity::class.java)  // We'll create this next
                "Automatic" -> Intent(requireContext(), MapDetailActivity::class.java)
                else -> Intent(requireContext(), DisplayEntryActivity::class.java)
            }
            intent.putExtra("entry_id", entry.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadEntries()
    }

    private fun loadEntries() {
        val sharedPreferences = requireContext().getSharedPreferences(
            "MyRuns_Preferences",
            Context.MODE_PRIVATE
        )
        val useMetric = sharedPreferences.getString("unit_preference", "metric") == "metric"

        lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) {
                repository.getAllEntries().sortedBy { it.dateTime }
            }

            // Create new adapter instance with current preference
            adapter = HistoryAdapter(entries, useMetric) { entry ->
                val intent = when (entry.inputType) {
                    "GPS" -> Intent(requireContext(), MapDetailActivity::class.java)  // We'll create this next
                    "Automatic" -> Intent(requireContext(), MapDetailActivity::class.java)
                    else -> Intent(requireContext(), DisplayEntryActivity::class.java)
                }
                intent.putExtra("entry_id", entry.id)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload entries when returning to this fragment
        loadEntries()
    }
}