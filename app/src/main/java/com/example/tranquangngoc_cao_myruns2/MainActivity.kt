package com.example.tranquangngoc_cao_myruns2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.tranquangngoc_cao_myruns2.HistoryFragment.HistoryFragment
import com.example.tranquangngoc_cao_myruns2.SettingFragment.SettingsFragment
import com.example.tranquangngoc_cao_myruns2.StartFragment.StartFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Set default unit preference if not set
        val sharedPreferences = getSharedPreferences("MyRuns_Preferences", MODE_PRIVATE);
        if (!sharedPreferences.contains("unit_preference")) {
            sharedPreferences.edit().putString("unit_preference", "metric").apply()
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "START"
                1 -> "HISTORY"
                2 -> "SETTINGS"
                else -> throw IllegalArgumentException("Invalid tab position")
            }
        }.attach()
    }

    private inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> StartFragment()
                1 -> HistoryFragment()
                2 -> SettingsFragment()
                else -> throw IllegalArgumentException("Invalid tab position")
            }
        }
    }
}