package com.nat.finalstoryapp.data.repository

import com.nat.finalstoryapp.data.api.ApiService
import com.nat.finalstoryapp.data.api.response.StoryResponse
import com.nat.finalstoryapp.data.pref.UserPreference
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService, private val userPreference: UserPreference
) {

    suspend fun getStories(token: String): StoryResponse {
        return try {
            apiService.getStories("Bearer $token")
        } catch (e: HttpException) {
            throw e
        }
    }

    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        return try {
            apiService.getStoriesWithLocation("Bearer $token")
        } catch (e: HttpException) {
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