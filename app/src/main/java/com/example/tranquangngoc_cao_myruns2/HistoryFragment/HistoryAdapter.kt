package com.example.tranquangngoc_cao_myruns2.HistoryFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tranquangngoc_cao_myruns2.Database.ExerciseEntry
import com.example.tranquangngoc_cao_myruns2.R
import com.example.tranquangngoc_cao_myruns2.Util.UnitConverter
import com.example.tranquangngoc_cao_myruns2.Util.Util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var entries: List<ExerciseEntry>,
    private val useMetric: Boolean,
    private val onItemClick: (ExerciseEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainInfo: TextView = view.findViewById(R.id.textViewMainInfo)
        val subInfo: TextView = view.findViewById(R.id.textViewSubInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val dateFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(entry.dateTime))

        // Format: "Manual Entry: Running, 19:15:00 Oct 24 2024"
        holder.mainInfo.text = "${entry.inputType}: ${entry.activityType}, $dateString"

        // Format distance based on unit preference
        val distance = if (useMetric) {
            UnitConverter.milesToKilometers(entry.distance)
        } else {
            entry.distance
        }
        val distanceUnit = if (useMetric) "Kilometers" else "Miles"
        val distanceFormatted = String.format("%.2f %s", distance, distanceUnit)

        // Format: "804.67 Kilometers, 0secs"
        holder.subInfo.text = "$distanceFormatted, ${Util.formatDuration(entry.duration)}"

        holder.itemView.setOnClickListener {
            onItemClick(entry)
        }
    }

    override fun getItemCount() = entries.size

    fun updateEntries(newEntries: List<ExerciseEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}