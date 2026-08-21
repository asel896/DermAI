package com.dermai

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    // vt baslat
    DatabaseFactory.init()

    val geminiService = GeminiService()
    val authService = AuthService()

    val jwtSecret = System.getenv("JWT_SECRET") ?: "dermai_secret_key_2026"
    val jwtIssuer = "http://0.0.0.0:8081/"
    val jwtAudience = "http://0.0.0.0:8081/users"

    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        // jwt kimlik dogrulama
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(
                    JWT.require(Algorithm.HMAC256(jwtSecret))
                        .withAudience(jwtAudience)
                        .withIssuer(jwtIssuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.getClaim("email").asString() != "") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }

        routing {
            get("/") {
                call.respondText("DermAI Backend Sunucusu Çalışıyor!")
            }

            get("/api/health") {
                call.respond(mapOf("status" to "success", "message" to "DermAI API erişilebilir durumda"))
            }

            // Auth
            route("/api/auth") {
                post("/register") {
                    try {
                        val request = call.receive<AuthRequest>()
                        val response = authService.register(request)
                        call.respond(HttpStatusCode.Created, response)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Kayıt başarısız")))
                    }
                }

                post("/login") {
                    try {
                        val request = call.receive<AuthRequest>()
                        val response = authService.login(request)
                        call.respond(HttpStatusCode.OK, response)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (e.message ?: "Giriş başarısız")))
                    }
                }
            }

            // metinle analiz
            post("/api/analyze") {
                val request = call.receive<AnalysisRequest>()
                try {
                    val geminiResponse = geminiService.analyzeIngredients(request.ingredientsText)
                    call.respondText(geminiResponse, contentType = ContentType.Application.Json)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.localizedMessage ?: "Hata oluştu")))
                }
            }

            // Fotoğraf/Görsel İle Analiz Rotası (Yeni Eklenen Endpoint)
            post("/api/analyze-image") {
                try {
                    val multipart = call.receiveMultipart()
                    var imageBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            imageBytes = part.streamProvider().readBytes()
                        }
                        part.dispose()
                    }

                    if (imageBytes != null) {
                        val geminiResponse = geminiService.analyzeImage(imageBytes!!)
                        call.respondText(geminiResponse, contentType = ContentType.Application.Json)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Lütfen geçerli bir görsel yükleyin."))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.localizedMessage ?: "Görsel analizi sırasında hata oluştu")))
                }
            }
        }
    }.start(wait = true)
}