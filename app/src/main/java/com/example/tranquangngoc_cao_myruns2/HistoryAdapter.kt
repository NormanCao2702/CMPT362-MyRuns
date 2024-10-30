package com.example.tranquangngoc_cao_myruns2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
            String.format("%.2f Kilometers", UnitConverter.milesToKilometers(entry.distance))
        } else {
            String.format("%.2f Miles", entry.distance)
        }

        // Format: "804.67 Kilometers, 0secs"
        holder.subInfo.text = "$distance, ${Util.formatDuration(entry.duration)}"

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