package com.example.dermai

import com.google.gson.annotations.SerializedName

// auth modelleri
data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String
)

// analiz modelleri
data class AnalysisRequest(
    @SerializedName("ingredientsText") val ingredientsText: String
)

data class IngredientDetail(
    @SerializedName("name") val name: String,
    @SerializedName("safetyScore") val safetyScore: Int,
    @SerializedName("purpose") val purpose: String,
    @SerializedName("isHarmful") val isHarmful: Boolean
)

data class AnalysisResponse(
    @SerializedName("overallRating") val overallRating: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("ingredients") val ingredients: List<IngredientDetail>
)

data class HistoryResponseItem(
    @SerializedName("id") val id: Int,
    @SerializedName("ingredientsText") val ingredientsText: String,
    @SerializedName("resultJson") val resultJson: String,
    @SerializedName("createdAt") val createdAt: String
)