package com.dermai

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


fun main() {
    val geminiService = GeminiService()
    val jsonParser = Json { ignoreUnknownKeys = true }

    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/") {
                call.respondText("DermAI Backend Sunucusu Çalışıyor!")
            }

            get("/api/health") {
                call.respond(mapOf("status" to "success", "message" to "DermAI API erişilebilir durumda"))
            }

            // Kozmetik İçerik Analiz Endpoint'i
            post("/api/analyze") {
                val request = call.receive<AnalysisRequest>()


                try {
                    // Gemini API'den yanıt al
                    val rawGeminiResponse = geminiService.analyzeIngredients(request.ingredientsText)

                    // Gemini'nin ürettiği metin yanıtı içerisindeki JSON'ı ayıkla
                    val geminiJsonText = jsonParser.parseToJsonElement(rawGeminiResponse)
                        .let { element ->
                            val candidates = element.toString()
                            rawGeminiResponse
                        }


                    call.respondText(rawGeminiResponse, contentType = io.ktor.http.ContentType.Application.Json)
                } catch (e: Exception) {
                    call.respond(mapOf("error" to "Analiz yapılırken hata oluştu: ${e.localizedMessage}"))
                }
            }
        }
    }.start(wait = true)
}