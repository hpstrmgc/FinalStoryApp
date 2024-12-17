package com.nat.finalstoryapp.ui.newstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nat.finalstoryapp.data.network.ApiConfig
import com.nat.finalstoryapp.data.api.response.FileUploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class NewStoryViewModel : ViewModel() {

    private val _fileUploadResponse = MutableLiveData<FileUploadResponse>()
    val fileUploadResponse: LiveData<FileUploadResponse> = _fileUploadResponse

    fun uploadStory(
        token: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: Float? = null,
        lon: Float? = null
    ) {
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.uploadStory("Bearer $token", description, photo, lat, lon)
                _fileUploadResponse.postValue(response)
            } catch (e: Exception) {
                _fileUploadResponse.postValue(
                    FileUploadResponse(
                        true, e.message ?: "Unknown error"
                    )
                )
            }
        }
    }
}