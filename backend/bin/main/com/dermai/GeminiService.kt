package com.dermai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileInputStream
import java.util.Base64
import java.util.Properties
import io.ktor.client.plugins.HttpTimeout

class GeminiService {
    private val apiKey: String = loadApiKey()
    private val client = HttpClient(CIO){
    install(HttpTimeout) {
        requestTimeoutMillis = 60000 // İsteğin tamamlanması için 60 saniye ver
        connectTimeoutMillis = 15000 // Bağlantı için 15 saniye ver
        socketTimeoutMillis = 60000  // Veri akışı için 60 saniye ver
    }
}
    private val jsonParser = Json { ignoreUnknownKeys = true }

    private fun loadApiKey(): String {
        
        val localPropertiesFile = File("local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties()
            properties.load(FileInputStream(localPropertiesFile))
            val key = properties.getProperty("GEMINI_API_KEY")
            if (!key.isNullOrBlank()) return key
        }
      
        return System.getenv("GEMINI_API_KEY") ?: ""
    }

    // metin analizi
    suspend fun analyzeIngredients(ingredientsText: String): String {
        val prompt = """
            Sen bir dermatolog ve kozmetik kimyagerisin. Aşağıda verilen INCI içerik listesini analiz et.
            Yanıtı SADECE ve SADECE geçerli bir JSON formatında ver. Yanıta asla markdown (```json ... ```) veya ekstra açıklama ekleme.
            
            İçerik Listesi: $ingredientsText
            
            JSON Formatı:
            {
              "overallRating": "Güvenli / Orta Riskli / Yüksek Riskli",
              "summary": "Kısa genel değerlendirme",
              "ingredients": [
                {
                  "name": "İçerik Adı",
                  "safetyScore": 1,
                  "purpose": "Kullanım Amacı",
                  "isHarmful": false
                }
              ]
            }
        """.trimIndent()

        return sendToGemini(prompt)
    }

    // görsel analizi
    suspend fun analyzeImage(imageBytes: ByteArray): String {
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        val prompt = "Bu kozmetik ürününün üzerindeki içerik (INCI) listesini oku ve dermatolojik analizini yap. Yanıtı SADECE yukarıdaki JSON formatında ver."

        val jsonPayload = """
            {
              "contents": [{
                "parts": [
                  { "text": ${objectToJsonString(prompt)} },
                  {
                    "inline_data": {
                      "mime_type": "image/jpeg",
                      "data": "$base64Image"
                    }
                  }
                ]
              }]
            }
        """.trimIndent()

        return executeRequest(jsonPayload)
    }

    private suspend fun sendToGemini(prompt: String): String {
        val jsonPayload = """
            {
              "contents": [{
                "parts": [{
                  "text": ${objectToJsonString(prompt)}
                }]
              }]
            }
        """.trimIndent()
        return executeRequest(jsonPayload)
    }

    private suspend fun executeRequest(jsonPayload: String): String {
        if (apiKey.isBlank()) {
            throw Exception("GEMINI_API_KEY tanımlanmamış! Lütfen local.properties dosyasını kontrol edin.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }

        val responseText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw Exception("Gemini API Hatası (${response.status.value}): $responseText")
        }

        val rootElement = jsonParser.parseToJsonElement(responseText).jsonObject
        val rawText = rootElement["candidates"]?.jsonArray?.get(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content ?: ""

        return rawText.replace("```json", "").replace("```", "").trim()
    }

    private fun objectToJsonString(value: String): String {
        return "\"" + value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "\""
    }
}