package com.dermai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GeminiService {
    private val apiKey = "AQ.Ab8RN6Il1v9ft2mGtPazSConhXI9intHRfn_TlwS7Ghrqw6BIA" // Kendi API anahtarın
    private val client = HttpClient(CIO)
    private val jsonParser = Json { ignoreUnknownKeys = true }

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

        val jsonPayload = """
            {
              "contents": [{
                "parts": [{
                  "text": ${objectToJsonString(prompt)}
                }]
              }]
            }
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }

        val responseText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception("Gemini API Hatası (${response.status.value}): $responseText")
        }

        // Gemini'nin ürettiği ham yanıtın içindeki metni (text) ayıklıyoruz
        val rootElement = jsonParser.parseToJsonElement(responseText).jsonObject
        val rawText = rootElement["candidates"]?.jsonArray?.get(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content ?: ""

        // Markdown kod bloklarını temizle (eğer Gemini eklediyse)
        return rawText.replace("```json", "").replace("```", "").trim()
    }

    private fun objectToJsonString(value: String): String {
        return "\"" + value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "\""
    }
}