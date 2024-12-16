package com.nat.finalstoryapp.ui.maps

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.nat.finalstoryapp.R
import com.nat.finalstoryapp.data.api.response.StoryResponse
import com.nat.finalstoryapp.data.di.Injection
import com.nat.finalstoryapp.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(Injection.provideRepository(this))
    }
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setMapStyle()
        observeStoriesWithLocation()

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun observeStoriesWithLocation() {
        val token = getTokenFromPreferences()
        if (token != null) {
            mapsViewModel.getStoriesWithLocation(token).observe(this) { storyResponse ->
                if (storyResponse != null) {
                    addMarkers(storyResponse)
                } else {
                    Log.e(TAG, "Failed to fetch stories with location")
                }
            }
        } else {
            Log.e(TAG, "Token is null")
        }
    }

    private fun getTokenFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("your_app_preferences", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    private fun addMarkers(storyResponse: StoryResponse) {
        storyResponse.listStory.forEach { story ->
            val lat = story.lat
            val lon = story.lon
            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                mMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description))
                boundsBuilder.include(latLng)
            }
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}