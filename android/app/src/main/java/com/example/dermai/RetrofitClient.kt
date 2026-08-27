package com.example.dermai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8081/"

    var userToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        userToken?.let { token ->
            builder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = builder.build()
        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // bağlantı zaman aşımı
        .readTimeout(60, TimeUnit.SECONDS)    // gemini yanıt bekleme zaman aşımı
        .writeTimeout(30, TimeUnit.SECONDS)   // veri gönderme zaman aşımı
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}