package com.nat.finalstoryapp.ui.newstory

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.nat.finalstoryapp.R
import com.nat.finalstoryapp.databinding.ActivityNewStoryBinding
import com.nat.finalstoryapp.utils.getImageUri
import com.nat.finalstoryapp.utils.reduceFileImage
import com.nat.finalstoryapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private var currentImageUri: Uri? = null
    private var tempImageUri: Uri? = null
    private val newStoryViewModel: NewStoryViewModel by viewModels()
    private var currentLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show()
            startCamera()
        } else {
            Toast.makeText(
                this, "Camera permission denied, you can change it in settings", Toast.LENGTH_LONG
            ).show()
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            binding.checkboxIncludeLocation.isChecked = false
            showLocationPermissionRationaleDialog()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this).setTitle("Camera Permission Required")
            .setMessage("This app needs camera access to take pictures. Please grant permission in the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showLocationPermissionRationaleDialog() {
        AlertDialog.Builder(this).setTitle("Location Permission Required")
            .setMessage("This app needs precise location access to add location to your story. Please grant permission in the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.buttonGallery.setOnClickListener { startGallery() }
        binding.buttonCamera.setOnClickListener {
            when {
                allPermissionsGranted() -> {
                    startCamera()
                }

                shouldShowRequestPermissionRationale(REQUIRED_PERMISSION) -> {
                    showPermissionRationaleDialog()
                }

                else -> {
                    requestPermissionLauncher.launch(REQUIRED_PERMISSION)
                }
            }
        }
        binding.buttonAdd.setOnClickListener { uploadImage() }

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.checkboxIncludeLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                when {
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        getCurrentLocation()
                    }

                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                        showLocationPermissionRationaleDialog()
                        binding.checkboxIncludeLocation.isChecked = false
                    }

                    else -> {
                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            } else {
                currentLocation = null
            }
        }

        newStoryViewModel.fileUploadResponse.observe(this) { response ->
            showLoading(false)
            if (response.error) {
                showToast(response.message)
            } else {
                showToast("Story uploaded successfully")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            tempImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = tempImageUri
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imagePreview.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.edAddDescription.text.toString()

            showLoading(true)
            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo", imageFile.name, requestImageFile
            )
            val token = getTokenFromPreferences()
            if (token != null) {
                val lat = currentLocation?.latitude?.toFloat()
                val lon = currentLocation?.longitude?.toFloat()
                newStoryViewModel.uploadStory(token, requestBody, multipartBody, lat, lon)
            } else {
                showLoading(false)
                showToast("Token is null")
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
                Toast.makeText(this, "Location retrieved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                binding.checkboxIncludeLocation.isChecked = false
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            binding.checkboxIncludeLocation.isChecked = false
        }
    }

    private fun getTokenFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("your_app_preferences", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}