package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File
import org.yaml.snakeyaml.Yaml


fun Application.configureRouting() {
    routing {
        staticFiles("/media", File("data")) {
            contentType { _ -> ContentType.Audio.MPEG }
        }
        get("/dialogs") {
            val dialogs = ExampleData(
                listOf(
                    buildExampleDialog(readSourceData("data/source.yaml")),
                    buildExampleDialog(readSourceData("data/source1.yaml")),
                    buildExampleDialog(readSourceData("data/source2.yaml"))
                )
            )

            call.respondText(Json.encodeToString(dialogs))
        }
    }
}

fun readSourceData(fileName : String): SourceData {
    val yamlFile = File(fileName)
    val yamlLoader = Yaml()
    val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())
    val mapper = ObjectMapper().registerKotlinModule()
    return mapper.convertValue(rawMap, SourceData::class.java)
}

fun buildExampleDialog(sourceData : SourceData) : ExampleDialog =
    ExampleDialog(
        id = sourceData.dialog.id,
        title = sourceData.dialog.title,
        icon = sourceData.dialog.icon,
        iconBg = sourceData.dialog.iconBg,
        progress = ExampleProgress("roles:1", "subjects:2", "flows:3"),
        description = sourceData.dialog.description,
        roles = sourceData.dialog.roles,
        variables = sourceData.dialog.variables?.let{
            ExampleVariables("subj",
                sourceData.dialog.variables.values.map{
                    ExampleVariableValue(
                        it.variableId,
                        ExampleTextWithAudio(
                            it.text,
                            translate(
                                "NL",
                                it.text),
                            "http://192.168.2.74:8080/media/voice/NL/${toHash(it.text)}")
                    )
                }
            )
        },
        flowsSelection = sourceData.dialog.flowsSelection?.let{
            listOf(
                ExampleFlowSelectionItem(
                    0,
                    sourceData.dialog.flowsSelection.row0.role,
                    listOf(
                        ExampleFlowSelectionText(
                            null,
                            sourceData.dialog.flowsSelection.row0.text,
                            translate("NL", sourceData.dialog.flowsSelection.row0.text)
                        )
                    )
                ),
                ExampleFlowSelectionItem(
                    1,
                    sourceData.dialog.flowsSelection.row1.role,
                    listOf(
                        ExampleFlowSelectionText(
                            sourceData.dialog.flowsSelection.row1.flowOptions[0].flowId,
                            sourceData.dialog.flowsSelection.row1.flowOptions[0].text,
                            translate("NL", sourceData.dialog.flowsSelection.row1.flowOptions[0].text)
                        ),
                        ExampleFlowSelectionText(
                            sourceData.dialog.flowsSelection.row1.flowOptions[1].flowId,
                            sourceData.dialog.flowsSelection.row1.flowOptions[1].text,
                            translate("NL", sourceData.dialog.flowsSelection.row1.flowOptions[1].text)
                        )
                    )
                )
            )
        },
        flows = sourceData.dialog.flows.map { ExampleFlow (
            it.id,
            it.sentences.map { it2 ->
                ExampleSentence(
                    it2.seq,
                    it2.role,
                    it2.multiText.map { it3 ->
                        ExampleSentenceText(
                            it3.variableId,
                            it3.text,
                            translate("NL", it3.text),
                            "http://192.168.2.74:8080/media/voice/NL/${toHash(it3.text)}"
                        )
                    }
                )
            }
        ) }
    )
