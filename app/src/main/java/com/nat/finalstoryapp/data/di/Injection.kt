package com.nat.finalstoryapp.data.di

import android.content.Context
import com.nat.finalstoryapp.data.database.StoryDatabase
import com.nat.finalstoryapp.data.network.ApiConfig
import com.nat.finalstoryapp.data.pref.UserPreference
import com.nat.finalstoryapp.data.repository.StoryRepository
import com.nat.finalstoryapp.utils.dataStore

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        val database = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(apiService, pref, database)
    }
}