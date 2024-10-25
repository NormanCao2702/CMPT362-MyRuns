package com.example.tranquangngoc_cao_myruns2

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MapDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_display)

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)

        buttonSave.setOnClickListener {
            // Handle save action
            finish()
        }

        buttonCancel.setOnClickListener {
            // Handle cancel action
            finish()
        }
    }
}