package com.example.tranquangngoc_cao_myruns2.startFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tranquangngoc_cao_myruns2.googleMap.MapDisplayActivity
import com.example.tranquangngoc_cao_myruns2.manualActivity.ManualEntryActivity
import com.example.tranquangngoc_cao_myruns2.R

class StartFragment : Fragment() {

    private lateinit var spinnerInputType: Spinner
    private lateinit var spinnerActivityType: Spinner
    private lateinit var buttonStart: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        spinnerInputType = view.findViewById(R.id.spinnerInputType)
        spinnerActivityType = view.findViewById(R.id.spinnerActivityType)
        buttonStart = view.findViewById(R.id.buttonStart)

        setupSpinners()
        setupStartButton()

        return view
    }

    private fun setupSpinners() {
        val inputTypes = arrayOf("Manual Entry", "GPS", "Automatic")
        val activityTypes = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other")

        spinnerInputType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, inputTypes)
        spinnerActivityType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityTypes)
    }

    private fun setupStartButton() {
        buttonStart.setOnClickListener {
            when (spinnerInputType.selectedItem.toString()) {
                "Manual Entry" -> {
                    val intent = Intent(requireContext(), ManualEntryActivity::class.java)
                    intent.putExtra("activity_type", spinnerActivityType.selectedItem.toString())
                    startActivity(intent)
                }
                "GPS"-> {
                    val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                    intent.putExtra("activity_type", spinnerActivityType.selectedItem.toString())
                    startActivity(intent)
                }
                "Automatic" -> {
                    val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                    intent.putExtra("input_type", "Automatic")
                    startActivity(intent)
                }
            }
        }
    }

}