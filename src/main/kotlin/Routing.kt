package com.example

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File
import org.example.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun configureRouting(app: Application) {
    DatabaseFactory.init()
    app.routing {
        staticFiles("/media", File("data")) {
            contentType { _ -> ContentType.Audio.MPEG }
        }
        get("/dialogs") {
            val dialogsList = buildDialogList()
            val response = ExampleData(dialogsList)
            call.respondText(Json.encodeToString(response))
        }
    }
}