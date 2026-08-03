package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private val practiceReadyUsers = ConcurrentHashMap.newKeySet<PracticeReadyRequest>()

fun configureRouting(app: Application) {
    DatabaseFactory.init()
    app.routing {
        singlePageApplication {
            applicationRoute = "app"
            filesPath = "spa"
            defaultPage = "index.html"
            useResources = false
        }
        staticFiles("/assets", File("spa/assets"))

        get("/dialogs") {
            val dialogsList = buildDialogList()
            val response = ExampleData(dialogsList)
            call.respondText(Json.encodeToString(response))
        }

        post("/practice/ready") {
            val request = call.receive<PracticeReadyRequest>()
            
            // Look for another user who wants to practice the same dialog
            val match = practiceReadyUsers.find { it.dialogId == request.dialogId && it.userId != request.userId }
            
            if (match != null) {
                // Match found: consume the match so they don't match with a third person
                practiceReadyUsers.remove(match)
                call.respond(PracticeReadyResponse(matchFound = true))
            } else {
                // No match found: add to the waitlist
                practiceReadyUsers.add(request)
                call.respond(PracticeReadyResponse(matchFound = false))
            }
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