package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun configureRouting(app: Application) {
    DatabaseFactory.init()
    app.routing {
        get("/dialogs") {
            val dialogsList = buildDialogList()
            val response = ExampleData(dialogsList)
            call.respondText(Json.encodeToString(response))
        }
        get("/media/voice/{lang}/{hash}") {
            val lang = call.parameters["lang"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val hash = call.parameters["hash"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val voiceData = transaction {
                VoiceTable.selectAll()
                    .where { (VoiceTable.lang eq lang) and (VoiceTable.hashEn eq hash) }
                    .singleOrNull()?.get(VoiceTable.data)
            }

            if (voiceData != null) {
                call.respondBytes(voiceData.bytes, ContentType.Audio.MPEG)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}