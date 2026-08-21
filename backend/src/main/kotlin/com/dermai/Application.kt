package com.dermai

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

fun main() {
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

                // örnek bir analiz çıktısı 
                val response = AnalysisResponse(
                    overallRating = "Güvenli",
                    summary = "Girdiğiniz içerik listesi genel olarak cilt bariyerini destekleyen ve güvenli bileşenlerden oluşmaktadır.",
                    ingredients = listOf(
                        IngredientDetail(
                            name = "Niacinamide",
                            safetyScore = 1,
                            purpose = "Cilt tonu eşitleme ve gözenek sıkılaştırma",
                            isHarmful = false
                        ),
                        IngredientDetail(
                            name = "Glycerin",
                            safetyScore = 1,
                            purpose = "Yoğun nemlendirme",
                            isHarmful = false
                        )
                    )
                )

                call.respond(response)
            }
        }
    }.start(wait = true)
}