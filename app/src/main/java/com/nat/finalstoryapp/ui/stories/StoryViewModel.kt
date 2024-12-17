package com.nat.finalstoryapp.ui.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nat.finalstoryapp.data.api.response.ListStoryItem
import com.nat.finalstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.flow.Flow


class StoryViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        return repository.getStories(token).cachedIn(viewModelScope)
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