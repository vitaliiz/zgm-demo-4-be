package com.example

import io.ktor.server.application.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson {}
    }
}