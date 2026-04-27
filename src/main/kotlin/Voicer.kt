package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.*
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.runBlocking
import java.io.File

fun textToSpeech(lang: String, voiceName: String, textEN: String, translated: String) {
    val hashEN = toHash(textEN)
    val voiceFile = File("./data/voice/$lang/$hashEN")
    if (!voiceFile.exists()) {
        val bytes = createAudio(translated, voiceName)
        voiceFile.writeBytes(bytes)
    }
}

fun createAudio(text: String, voiceName: String) : ByteArray {
    return runBlocking {
        val client = HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.INFO
            }
        }
        val data: String =  """
            <speak version='1.0' xml:lang='en-US'>
                <voice name='$voiceName'>
                    $text
                </voice> 
            </speak>
        """.trimMargin()


        val resp = client.post("https://francecentral.tts.speech.microsoft.com/cognitiveservices/v1") {
            header("Ocp-Apim-Subscription-Key", "056c89324d154070aefdc8ca5409e07a")
            header("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3")
            header("Content-Type", "application/ssml+xml")
            setBody(
                data
            )
        }

        if (resp.status != HttpStatusCode.OK) throw Exception("Failed to translate text: $text. HTTP response: ${resp.status}")

        resp.bodyAsChannel().readRemaining().readBytes()
    }
}
