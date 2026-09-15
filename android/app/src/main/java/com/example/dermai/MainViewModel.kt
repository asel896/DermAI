package com.example.dermai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel : ViewModel() {

    private val _analysisState = MutableStateFlow<UiState<AnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<AnalysisResponse>> = _analysisState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<List<HistoryResponseItem>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<HistoryResponseItem>>> = _historyState.asStateFlow()

    fun analyzeText(ingredientsText: String) {
        if (ingredientsText.isBlank()) {
            _analysisState.value = UiState.Error("Lütfen analiz edilecek içerik metnini girin.")
            return
        }

        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.analyzeText(
                    AnalysisRequest(ingredientsText = ingredientsText)
                )

                if (response.isSuccessful && response.body() != null) {
                    _analysisState.value = UiState.Success(response.body()!!)

                    fetchHistory()
                } else {
                    _analysisState.value = UiState.Error("Analiz başarısız (${response.code()}): ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(e.localizedMessage ?: "Sunucu hatası oluştu.")
            }
        }
    }

    fun analyzeImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            try {
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("image", "analysis_image.jpg", requestFile)

                val response = RetrofitClient.apiService.analyzeImage(multipartBody)

                if (response.isSuccessful && response.body() != null) {
                    _analysisState.value = UiState.Success(response.body()!!)
                    fetchHistory()
                } else {
                    _analysisState.value = UiState.Error("Görsel analizi başarısız: ${response.code()}")
                }
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası oluştu.")
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.getHistory()

                if (response.isSuccessful && response.body() != null) {
                    _historyState.value = UiState.Success(response.body()!!)
                } else {
                    _historyState.value = UiState.Error("Geçmiş verileri alınamadı: ${response.code()}")
                }
            } catch (e: Exception) {
                _historyState.value = UiState.Error(e.localizedMessage ?: "Ağ hatası oluştu.")
            }
        }
    }

    fun resetAnalysisState() {
        _analysisState.value = UiState.Idle
    }
}