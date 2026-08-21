package com.dermai

import kotlinx.serialization.Serializable

// Mobil uygulamadan gelen veri
@Serializable
data class AnalysisRequest(
    val ingredientsText: String // Water, Glycerin, Niacinamide, Salicylic Acid
)

// Analiz sonucu oluşturulan içerik detayı
@Serializable
data class IngredientDetail(
    val name: String,         // İçerik adı (Niacinamide)
    val safetyScore: Int,     // Güvenlik puanı (1-10)
    val purpose: String,      // Kullanım amacı (Leke karşıtı)
    val isHarmful: Boolean    // Riskli/Zararlı mı
)

// Mobil uygulamaya döneceğimiz yanıt
@Serializable
data class AnalysisResponse(
    val overallRating: String,              // Güvenli, Orta Riskli
    val summary: String,                    // Genel özet yorumu
    val ingredients: List<IngredientDetail> // Detaylı içerik listesi
)