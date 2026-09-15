package com.dermai

import kotlinx.serialization.Serializable


@Serializable
data class AnalysisRequest(
    val ingredientsText: String
)
@Serializable
data class IngredientDetail(
    val name: String,         // İçerik adı
    val safetyScore: Int,     // Güvenlik puanı
    val purpose: String,      // Kullanım amacı
    val isHarmful: Boolean    // Zararlı mı
)

@Serializable
data class HistoryResponseItem(
    val id: Int,
    val ingredientsText: String,
    val resultJson: String,
    val createdAt: String
)


@Serializable
data class AnalysisResponse(
    val overallRating: String,              // Güvenli, Riskli
    val summary: String,                    // Genel özet
    val ingredients: List<IngredientDetail> // içerik listesi
)