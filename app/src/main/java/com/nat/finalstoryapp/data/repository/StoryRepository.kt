package com.nat.finalstoryapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nat.finalstoryapp.data.api.response.ListStoryItem
import com.nat.finalstoryapp.data.api.response.StoryResponse
import com.nat.finalstoryapp.data.database.StoryDatabase
import com.nat.finalstoryapp.data.network.ApiService
import com.nat.finalstoryapp.data.pref.UserPreference
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).flow
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

        fun getInstance(apiService: ApiService, userPreference: UserPreference, storyDatabase: StoryDatabase): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference, storyDatabase).also { instance = it }
            }
    }
}