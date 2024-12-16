package com.nat.finalstoryapp.data.di

import android.content.Context
import com.nat.finalstoryapp.data.api.ApiConfig
import com.nat.finalstoryapp.data.pref.UserPreference
import com.nat.finalstoryapp.data.repository.StoryRepository
import com.nat.finalstoryapp.utils.dataStore

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(apiService, pref)
    }
}