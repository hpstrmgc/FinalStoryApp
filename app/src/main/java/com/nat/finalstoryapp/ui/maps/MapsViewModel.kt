package com.nat.finalstoryapp.ui.maps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.nat.finalstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStoriesWithLocation(token: String) = liveData(Dispatchers.IO) {
        try {
            val stories = repository.getStoriesWithLocation(token)
            emit(stories)
        } catch (e: Exception) {
            Log.e("MapsViewModel", "Error fetching stories with location", e)
            emit(null)
        }
    }
}