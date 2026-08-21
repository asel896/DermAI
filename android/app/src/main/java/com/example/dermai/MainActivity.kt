package com.example.dermai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Text(text = "DermAI Test Çalışıyor! Logcat'i kontrol et.")
        }

        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("DermAI_TEST", "--- BAŞARILI GİRİŞ/KAYIT ---")
                        Log.d("DermAI_TEST", "Token: ${state.data.token}")

                        Log.d("DermAI_TEST", "Gemini Analizi Başlatılıyor...")
                        viewModel.analyzeText("Water, Glycerin, Niacinamide, Salicylic Acid")
                    }
                    is UiState.Error -> Log.e("DermAI_TEST", "Auth Hatası: ${state.message}")
                    is UiState.Loading -> Log.d("DermAI_TEST", "İşlem Yükleniyor...")
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.analysisState.collect { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("DermAI_TEST", "--- GEMİNİ ANALİZ BAŞARILI ---")
                        Log.d("DermAI_TEST", "Özet: ${state.data.summary}")
                        Log.d("DermAI_TEST", "Genel Skor: ${state.data.overallRating}")
                    }
                    is UiState.Error -> Log.e("DermAI_TEST", "Analiz Hatası: ${state.message}")
                    is UiState.Loading -> Log.d("DermAI_TEST", "Analiz Yükleniyor...")
                    else -> {}
                }
            }
        }

        val testEmail = "user_${System.currentTimeMillis()}@gmail.com"
        Log.d("DermAI_TEST", "Yeni kayıt deneniyor: $testEmail")
        viewModel.register(testEmail, "123456")
    }
}