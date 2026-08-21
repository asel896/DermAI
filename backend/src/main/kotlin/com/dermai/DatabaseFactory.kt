package com.dermai

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}

object HistoryTable : Table("analysis_history") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id)
    val ingredientsText = text("ingredients_text")
    val resultJson = text("result_json")
    val createdAt = varchar("created_at", 50)

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "dermai_db"
        val user = System.getenv("DB_USER") ?: "postgres"
        val password = System.getenv("DB_PASSWORD") ?: ""

        val jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"
        val driverClassName = "org.postgresql.Driver"

        val database = Database.connect(jdbcUrl, driverClassName, user, password)

        transaction(database) {
            SchemaUtils.create(UsersTable, HistoryTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction { block() }
}