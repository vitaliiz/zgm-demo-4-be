package org.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcUrl = "jdbc:postgresql://localhost:5432/zgm"
        val user = "postgres"
        val password = "pass"
        Database.connect(jdbcUrl, driverClassName, user, password)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                WordTable, RoleTable, SentenceTable, DialogTable, DialogFlowStepSentenceTable, TranslationTable, VoiceTable
            )
        }
    }
}