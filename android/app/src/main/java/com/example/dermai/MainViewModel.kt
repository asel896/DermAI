package com.example.dermai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val tokenManager = TokenManager(application)

    private val _authState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val authState: StateFlow<UiState<AuthResponse>> = _authState

    private val _analysisState = MutableStateFlow<UiState<AnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<AnalysisResponse>> = _analysisState

    private val _historyState = MutableStateFlow<UiState<List<HistoryResponseItem>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<HistoryResponseItem>>> = _historyState

    init {
        viewModelScope.launch {
            tokenManager.getToken.collect { token ->
                RetrofitClient.userToken = token
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.login(AuthRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager.saveToken(authData.token)
                    _authState.value = UiState.Success(authData)
                } else {
                    val err = response.errorBody()?.string() ?: "Giriş başarısız."
                    _authState.value = UiState.Error("HTTP ${response.code()}: $err")
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.register(AuthRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager.saveToken(authData.token)
                    _authState.value = UiState.Success(authData)
                } else {
                    val err = response.errorBody()?.string() ?: "Kayıt başarısız."
                    _authState.value = UiState.Error("HTTP ${response.code()}: $err")
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun analyzeText(text: String) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.analyzeText(AnalysisRequest(text))
                if (response.isSuccessful && response.body() != null) {
                    _analysisState.value = UiState.Success(response.body()!!)
                } else {
                    val err = response.errorBody()?.string() ?: "Analiz hatası."
                    _analysisState.value = UiState.Error("HTTP ${response.code()}: $err")
                }
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun analyzeImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            try {
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", "ingredient.jpg", requestFile)

                val response = RetrofitClient.apiService.analyzeImage(body)
                if (response.isSuccessful && response.body() != null) {
                    _analysisState.value = UiState.Success(response.body()!!)
                } else {
                    _analysisState.value = UiState.Error("Görsel analizi başarısız.")
                }
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası")
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
                    _historyState.value = UiState.Error("Geçmiş verisi alınamadı.")
                }
            } catch (e: Exception) {
                _historyState.value = UiState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _authState.value = UiState.Idle
        }
    }
}