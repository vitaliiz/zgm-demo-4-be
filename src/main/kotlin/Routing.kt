package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.http.content.*
import io.ktor.utils.io.*

private val practiceReadyUsers = ConcurrentHashMap.newKeySet<PracticeReadyRequest>()
private val userSessions = ConcurrentHashMap<String, WebSocketServerSession>()
private val activeDialogs = ConcurrentHashMap<String, String>()

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

        webSocket("/ws/{userId}") {
            val userId = call.parameters["userId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing userId"))
            userSessions[userId] = this
            try {
                for (frame in incoming) {
                    // Handle incoming messages if needed
                }
            } finally {
                userSessions.remove(userId)
                val partnerId = activeDialogs.remove(userId)
                if (partnerId != null) {
                    activeDialogs.remove(partnerId)
                }
            }
        }

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

                // Record the pairing
                activeDialogs[request.userId] = match.userId
                activeDialogs[match.userId] = request.userId

                // Notify the partner who was already waiting
                val partnerSession = userSessions[match.userId]
                if (partnerSession != null) {
                    val partnerResponse = PracticeReadyResponse(matchFound = true, dialogId = match.dialogId)
                    partnerSession.send(Frame.Text(Json.encodeToString(partnerResponse)))
                }

                call.respond(PracticeReadyResponse(matchFound = true, dialogId = match.dialogId))
            } else {
                // No match found: add to the waitlist
                practiceReadyUsers.add(request)
                call.respond(PracticeReadyResponse(matchFound = false, dialogId = ""))
            }
        }

        post("/practice/sentence") {
            val request = call.receive<PracticeSentenceRequest>()
            
            val partnerId = activeDialogs[request.userId]
            if (partnerId != null) {
                val partnerSession = userSessions[partnerId]
                if (partnerSession != null) {
                    val event = PracticeSentenceEvent(
                        userId = request.userId,
                        audioBase64 = request.audioBase64
                    )
                    partnerSession.send(Frame.Text(Json.encodeToString(event)))
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Partner session not found")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Partner not found")
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