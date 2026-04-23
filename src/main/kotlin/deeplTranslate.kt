package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentType
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DeeplRequest(
    val text: List<String>,
    val context: String,
    val source_lang: String,
    val target_lang: String
)

@Serializable
data class DeeplResponseItem(val detected_source_language: String, val text: String)
@Serializable
data class DeeplResponse(val translations: List<DeeplResponseItem>)


fun deeplTranslate(text: String, context: String, targetLang: String): String {
    return runBlocking {
        val client = HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                gson()
            }
        }
        val resp = client.post("https://api-free.deepl.com/v2/translate") {
            header("Authorization", "DeepL-Auth-Key f229ada0-cce6-4e2e-ae55-8ef1d86f6267:fx")
            contentType(ContentType.Application.Json)
            setBody(
                DeeplRequest(
                    text = listOf(text),
                    context = context,
                    source_lang = "EN",
                    target_lang = targetLang)
            )
        }
        Json.decodeFromString<DeeplResponse>(resp.bodyAsText()).translations[0].text
    }
}
