package com.example.dermai

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/analyze")
    suspend fun analyzeText(@Body request: AnalysisRequest): Response<AnalysisResponse>

    @Multipart
    @POST("api/analyze-image")
    suspend fun analyzeImage(@Part image: MultipartBody.Part): Response<AnalysisResponse>

    @GET("api/history")
    suspend fun getHistory(): Response<List<HistoryResponseItem>>
}