package com.nat.finalstoryapp.ui.stories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.nat.finalstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories(token: String) = liveData(Dispatchers.IO) {
        try {
            val stories = repository.getStories(token)
            emit(stories)
        } catch (e: HttpException) {
            Log.e("StoryViewModel", "Error fetching stories", e)
            emit(null)
        }
    }
}

class StoryViewModelFactory(private val repository: StoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return StoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}