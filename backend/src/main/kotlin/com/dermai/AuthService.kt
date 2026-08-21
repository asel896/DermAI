package com.dermai

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*

class AuthService {
    private val jwtSecret = System.getenv("JWT_SECRET") ?: "dermai_secret_key_2026"
    private val issuer = "http://0.0.0.0:8081/"
    private val audience = "http://0.0.0.0:8081/users"

    suspend fun register(request: AuthRequest): AuthResponse {
        val existingUser = DatabaseFactory.dbQuery {
            UsersTable.select { UsersTable.email eq request.email }.singleOrNull()
        }

        if (existingUser != null) {
            throw IllegalArgumentException("Bu e-posta adresi zaten kullanımda.")
        }

        val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

        val userId = DatabaseFactory.dbQuery {
            UsersTable.insert {
                it[email] = request.email
                it[passwordHash] = hashedPassword
            } get UsersTable.id
        }

        val token = generateToken(userId, request.email)
        return AuthResponse(token, request.email)
    }

    suspend fun login(request: AuthRequest): AuthResponse {
        val user = DatabaseFactory.dbQuery {
            UsersTable.select { UsersTable.email eq request.email }.singleOrNull()
        } ?: throw IllegalArgumentException("E-posta veya şifre hatalı.")

        val hashedPassword = user[UsersTable.passwordHash]
        val result = BCrypt.verifyer().verify(request.password.toCharArray(), hashedPassword)

        if (!result.verified) {
            throw IllegalArgumentException("E-posta veya şifre hatalı.")
        }

        val token = generateToken(user[UsersTable.id], user[UsersTable.email])
        return AuthResponse(token, user[UsersTable.email])
    }

    private fun generateToken(userId: Int, email: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 Gün geçerli
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}