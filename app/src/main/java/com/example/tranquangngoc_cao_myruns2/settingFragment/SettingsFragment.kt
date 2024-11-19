package com.example.tranquangngoc_cao_myruns2.settingFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import android.widget.EditText
import android.net.Uri
import com.example.tranquangngoc_cao_myruns2.R
import com.example.tranquangngoc_cao_myruns2.userProfile.UserProfileActivity

class SettingsFragment: Fragment()  {

    private lateinit var layoutUserProfile: LinearLayout
    private lateinit var checkBoxPrivacy: CheckBox
    private lateinit var layoutUnitPreference: LinearLayout
    private lateinit var layoutPrivacySetting: LinearLayout
    private lateinit var layoutComments: LinearLayout
    private lateinit var layoutWebpage: LinearLayout
    private lateinit var textViewUnitPreference: TextView
    private lateinit var textViewComments: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        layoutUserProfile = view.findViewById(R.id.layoutUserProfile)
        layoutPrivacySetting = view.findViewById(R.id.layoutPrivacySetting)
        checkBoxPrivacy = view.findViewById(R.id.checkBoxPrivacy)
        layoutUnitPreference = view.findViewById(R.id.layoutUnitPreference)
        layoutComments = view.findViewById(R.id.layoutComments)
        layoutWebpage = view.findViewById(R.id.layoutWebpage)
        textViewUnitPreference = view.findViewById(R.id.textViewUnitPreference)
        textViewComments = view.findViewById(R.id.textViewComments)

        setupClickListeners()
//        loadSettings()

        return view
    }

    private fun setupClickListeners() {
        layoutUserProfile.setOnClickListener {
            // Navigate to User Profile activity
            val intent = Intent(activity, UserProfileActivity::class.java)
            startActivity(intent)
        }

        layoutPrivacySetting.setOnClickListener {
            checkBoxPrivacy.isChecked = !checkBoxPrivacy.isChecked
            // TODO: Save the privacy setting
            savePrivacySetting(checkBoxPrivacy.isChecked)
        }

        layoutUnitPreference.setOnClickListener {
            // TODO: Implement Unit Preference selection
            showUnitPreferenceDialog()
        }

        layoutComments.setOnClickListener {
            // TODO: Implement Comments input
            showCommentsDialog()
        }

        layoutWebpage.setOnClickListener {
            // TODO: Implement opening webpage in browser
            openWebpage()
        }
    }

    // setupClickListeners for Main Button
    private fun showUnitPreferenceDialog() {
        val units = arrayOf("Metric (Kilometers)", "Imperial (Miles)")
        val checkedItem = if (loadUnitPreference() == "metric") 0 else 1

        AlertDialog.Builder(requireContext())
            .setTitle("Unit Preference")
            .setSingleChoiceItems(units, checkedItem) { dialog, which ->
                val selectedUnit = if (which == 0) "metric" else "imperial"
                saveUnitPreference(selectedUnit)
//                updateUnitPreferenceDisplay(selectedUnit)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCommentsDialog() {
        val input = EditText(requireContext())
        input.setText(loadComments())

        AlertDialog.Builder(requireContext())
            .setTitle("Comments")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val comment = input.text.toString()
                saveComments(comment)
//                updateCommentsDisplay(comment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openWebpage() {
        val url = "https://www.sfu.ca/computing.html"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // Helpers for ShowUnitPreference
    private fun saveUnitPreference(unit: String) {
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("unit_preference", unit)?.apply()
    }

    private fun loadUnitPreference(): String {
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("unit_preference", "metric") ?: "metric"
    }

//    private fun updateUnitPreferenceDisplay(unit: String) {
//        textViewUnitPreference.text = "Unit Preference"
//    }

    // Helpers for SaveComment
    private fun saveComments(comments: String) {
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("comments", comments)?.apply()
    }

    private fun loadComments(): String {
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("comments", "") ?: ""
    }

//    private fun updateCommentsDisplay(comments: String) {
//        textViewComments.text = "Please enter your comments"
//    }

    private fun savePrivacySetting(isChecked: Boolean) {
        // Save the privacy setting to SharedPreferences
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putBoolean("privacy_setting", isChecked)?.apply()
    }

    private fun loadPrivacySetting() {
        val sharedPreferences = activity?.getSharedPreferences("MyRuns_Preferences", Context.MODE_PRIVATE)
        val isPrivate = sharedPreferences?.getBoolean("privacy_setting", false) ?: false
        checkBoxPrivacy.isChecked = isPrivate
    }

    override fun onResume() {
        super.onResume()
        loadPrivacySetting()
    }

//    private fun loadSettings() {
//        updateUnitPreferenceDisplay(loadUnitPreference())
//        updateCommentsDisplay(loadComments())
//    }

}