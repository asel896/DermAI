package com.example.dermai

import com.google.gson.annotations.SerializedName

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class AuthRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val passwordHash: String
)

data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("email")
    val email: String? = null
)

data class AnalysisRequest(
    @SerializedName("ingredientsText")
    val ingredientsText: String
)

data class HistoryResponseItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("ingredientsText")
    val ingredientsText: String,
    @SerializedName("resultJson")
    val resultJson: String,
    @SerializedName("createdAt")
    val createdAt: String
)

data class IngredientDetail(
    @SerializedName("name")
    val name: String,
    @SerializedName("safetyScore")
    val safetyScore: Int,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("isHarmful")
    val isHarmful: Boolean
)

data class AnalysisResponse(
    @SerializedName("overallRating")
    val overallRating: String,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("ingredients")
    val ingredients: List<IngredientDetail>
)