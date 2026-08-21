package com.example.dermai

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    val email: String,
    val passwordHash: String
)

data class AuthResponse(
    val token: String,
    val email: String
)

data class AnalysisRequest(
    val ingredientsText: String
)

data class HistoryResponseItem(
    val id: Int,
    val ingredientsText: String,
    val resultJson: String,
    val createdAt: String
)

data class IngredientDetail(
    val name: String,
    val safetyScore: Int,
    val purpose: String,
    val isHarmful: Boolean
)

data class AnalysisResponse(
    val overallRating: String,
    val summary: String,
    val ingredients: List<IngredientDetail>
)