package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File
import org.yaml.snakeyaml.Yaml


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/gson") {
                call.respond(mapOf("hello" to "world"))
            }
        get("/dialogs") {
            val yamlFile = File("data/source.yaml")
            val yamlLoader = Yaml()
            val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())
            val mapper = ObjectMapper().registerKotlinModule()
            val parsedData = mapper.convertValue(rawMap, SourceData::class.java)

            val dialogs = ExampleData(
                listOf(
                    ExampleDialog(
                        id = "0",
                        title = parsedData.dialog.title,
                        icon = parsedData.dialog.icon,
                        iconBg = parsedData.dialog.iconBg,
                        progress = ExampleProgress("roles:1", "subjects:2", "flows:3"),
                        language = "NL",
                        totalProgress = "5%",
                        description = parsedData.dialog.description,
                        roles = parsedData.dialog.roles,
                        variables = ExampleVariables("subj",
                            parsedData.dialog.variables.values.map{
                                ExampleVariableValue(
                                    it.variableId,
                                    ExampleTextWithAudio(
                                        it.text,
                                        translate(
                                            "NL",
                                            it.text),
                                        "http://audiourl")
                                )
                            }
                        ),
                        flowsSelection = listOf(
                            ExampleFlowSelectionItem(
                                0,
                                parsedData.dialog.flowsSelection.row0.role,
                                listOf(
                                    ExampleFlowSelectionText(
                                    null,
                                    parsedData.dialog.flowsSelection.row0.text,
                                    translate("NL", parsedData.dialog.flowsSelection.row0.text)
                                    )
                                )
                            ),
                            ExampleFlowSelectionItem(
                                1,
                                parsedData.dialog.flowsSelection.row1.role,
                                listOf(
                                    ExampleFlowSelectionText(
                                        parsedData.dialog.flowsSelection.row1.flowOptions[0].flowId,
                                        parsedData.dialog.flowsSelection.row1.flowOptions[0].text,
                                        translate("NL", parsedData.dialog.flowsSelection.row1.flowOptions[0].text)
                                    ),
                                    ExampleFlowSelectionText(
                                        parsedData.dialog.flowsSelection.row1.flowOptions[1].flowId,
                                        parsedData.dialog.flowsSelection.row1.flowOptions[1].text,
                                        translate("NL", parsedData.dialog.flowsSelection.row1.flowOptions[1].text)
                                    )
                                )
                            )
                        ),
                        flows = parsedData.dialog.flows.map { ExampleFlow (
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
                                            "http://audiourl"
                                        )
                                    }
                                )
                            }
                        ) }
                    )
                )
            )

            call.respondText(Json.encodeToString(dialogs))
        }
    }
}