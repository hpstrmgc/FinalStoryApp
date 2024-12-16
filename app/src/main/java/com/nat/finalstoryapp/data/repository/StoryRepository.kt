package com.nat.finalstoryapp.data.repository

import android.util.Log
import com.nat.finalstoryapp.data.api.ApiService
import com.nat.finalstoryapp.data.api.response.StoryResponse
import com.nat.finalstoryapp.data.pref.UserPreference
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService, private val userPreference: UserPreference
) {

    suspend fun getStories(token: String): StoryResponse {
        return try {
            Log.d("StoryRepository", "Fetching stories with token: $token")
            apiService.getStories("Bearer $token")
        } catch (e: HttpException) {
            Log.e("StoryRepository", "Error fetching stories", e)
            throw e
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference).also { instance = it }
            }
    }
}