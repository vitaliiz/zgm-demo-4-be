package com.example

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureHttp() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson {}
    }
}

fun Application.configureRouting() {
    configureRouting(this)
}