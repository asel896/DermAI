package com.dermai

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryService {

    suspend fun saveAnalysis(userId: Int, ingredientsText: String, resultJson: String) {
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        DatabaseFactory.dbQuery {
            HistoryTable.insert {
                it[HistoryTable.userId] = userId
                it[HistoryTable.ingredientsText] = ingredientsText
                it[HistoryTable.resultJson] = resultJson
                it[HistoryTable.createdAt] = currentTime
            }
        }
    }

    suspend fun getUserHistory(userId: Int): List<HistoryResponseItem> {
        return DatabaseFactory.dbQuery {
            HistoryTable.select { HistoryTable.userId eq userId }
                .map {
                    HistoryResponseItem(
                        id = it[HistoryTable.id],
                        ingredientsText = it[HistoryTable.ingredientsText],
                        resultJson = it[HistoryTable.resultJson],
                        createdAt = it[HistoryTable.createdAt]
                    )
                }
        }
    }
}