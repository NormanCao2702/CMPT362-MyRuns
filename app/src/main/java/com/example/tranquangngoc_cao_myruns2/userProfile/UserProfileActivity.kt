package com.example.tranquangngoc_cao_myruns2.userProfile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.tranquangngoc_cao_myruns2.R
import com.example.tranquangngoc_cao_myruns2.util.Util

class UserProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var changePhotoButton: Button
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var classEditText: TextInputEditText
    private lateinit var majorEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var toolbar: Toolbar
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private val tempImgFileName = "xd_temp_img.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_user_profile)

        // Assign each component to its corresponding view in the layout xml
        profileImageView = findViewById(R.id.profile_photo)
        changePhotoButton = findViewById(R.id.btn_change_photo)
        nameEditText = findViewById(R.id.et_name)
        emailEditText = findViewById(R.id.et_email)
        phoneEditText = findViewById(R.id.et_phone)
        classEditText = findViewById(R.id.et_class)
        majorEditText = findViewById(R.id.et_major)
        saveButton = findViewById(R.id.btn_save)
        cancelButton = findViewById(R.id.btn_cancel)
        genderRadioGroup = findViewById(R.id.rg_gender)
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = Util.getBitmap(this, tempImgUri)
                profileImageView.setImageBitmap(bitmap)
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    }
                    profileImageView.setImageBitmap(bitmap)
                    saveProfileImageUri(uri.toString())
                }
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon?.setTint(Color.BLACK)

        Util.checkPermissions(this)

        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        tempImgUri = FileProvider.getUriForFile(this, "com.example.tranquangngoc_cao_myruns2.fileprovider", tempImgFile)

        loadProfile()
        setupListeners()
    }

    private fun saveProfileImageUri(uriString: String) {
        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_uri", uriString).apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                // Or navigate to settings: startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners(){
        // Event listener for the save button
        saveButton.setOnClickListener {
            saveProfile()
        }

        // Event listener for the Change Photo button
        changePhotoButton.setOnClickListener{
            // camera logic
            showPhotoSelectionDialog()
        }

        // Event listener for cancel button
        cancelButton.setOnClickListener{
            finish()
        }

    }

    private fun showPhotoSelectionDialog() {
        val options = arrayOf("Open Camera", "Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Pick Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun saveProfile(){
        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val selectedGenderID = genderRadioGroup.checkedRadioButtonId
        editor.putString("name", nameEditText.text.toString())
        editor.putString("email", emailEditText.text.toString())
        editor.putString("phone", phoneEditText.text.toString())
        editor.putString("class", classEditText.text.toString())
        editor.putString("major", majorEditText.text.toString())
        editor.putInt("gender", selectedGenderID)
        editor.putString("profile_image_uri", tempImgUri.toString())
        editor.apply()
        finish()
    }

    private fun loadProfile(){
        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // Fetch stored data from SharedPreferences, providing defaults if nothing is saved
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val classNum = sharedPreferences.getString("class", "")
        val major = sharedPreferences.getString("major", "")
        val gender = sharedPreferences.getInt("gender", -1)  // -1 indicates no selection
        val savedImageUri = sharedPreferences.getString("profile_image_uri", null)

//        if (savedImageUri != null) {
//            val savedUri = Uri.parse(savedImageUri)
//            val bitmap = Util.getBitmap(this, savedUri)
//            profileImageView.setImageBitmap(bitmap)
//        }

        if (savedImageUri != null) {
            val uri = Uri.parse(savedImageUri)
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                profileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the error, maybe set a default image
            }
        }

        // Set text fields if the data is not null
        nameEditText.setText(name ?: "")
        emailEditText.setText(email ?: "")
        phoneEditText.setText(phone ?: "")
        classEditText.setText(classNum ?: "")
        majorEditText.setText(major ?: "")

        // Directly check gender selection
        if (gender != -1) {
            genderRadioGroup.check(gender)
        } else {
            genderRadioGroup.clearCheck()  // Clear the selection if no valid selection exists
        }

        if (savedImageUri != null) {
            val savedUri = Uri.parse(savedImageUri)
            val bitmap = Util.getBitmap(this, savedUri)
            profileImageView.setImageBitmap(bitmap)
        }
    }
}