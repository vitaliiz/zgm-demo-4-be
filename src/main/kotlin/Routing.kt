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

fun Application.configureRouting() {
    DatabaseFactory.init()
    routing {
        staticFiles("/media", File("data")) {
            contentType { _ -> ContentType.Audio.MPEG }
        }
        get("/dialogs") {
            val dialogsList = transaction {
                val rolesMap = (RoleTable innerJoin WordTable).selectAll().associate {
                    it[RoleTable.id].value to it[WordTable.word]
                }

                DialogTable.selectAll().map { dialogRow ->
                    val dialogId = dialogRow[DialogTable.id].value
                    
                    val roleA = rolesMap[dialogRow[DialogTable.roleAId].value] ?: "Role A"
                    val roleB = rolesMap[dialogRow[DialogTable.roleBId].value] ?: "Role B"

                    // Fetch flows for this dialog
                    val flowRecords = (DialogFlowStepSentenceTable innerJoin SentenceTable)
                        .select { DialogFlowStepSentenceTable.dialogId eq dialogId }
                        .orderBy(DialogFlowStepSentenceTable.flowId to SortOrder.ASC, DialogFlowStepSentenceTable.step to SortOrder.ASC)
                        .toList()

                    val flows = flowRecords.groupBy { it[DialogFlowStepSentenceTable.flowId] }.map { (flowId, flowRows) ->
                        val sentences = flowRows.groupBy { it[DialogFlowStepSentenceTable.step] to it[DialogFlowStepSentenceTable.roleId] }
                            .map { (key, stepRoleRows) ->
                                val (step, roleId) = key
                                ExampleSentence(
                                    seq = step,
                                    role = rolesMap[roleId.value] ?: "Unknown",
                                    text = stepRoleRows.map { row ->
                                        val nativeText = row[SentenceTable.text]
                                        ExampleSentenceText(
                                            variableId = row[DialogFlowStepSentenceTable.wordId].value,
                                            native = nativeText,
                                            foreign = translate("NL", nativeText),
                                            audioUrl = "http://192.168.2.74:8080/media/voice/NL/${toHash(nativeText)}"
                                        )
                                    }
                                )
                            }
                        ExampleFlow(id = flowId, sentences = sentences)
                    }

                    ExampleDialog(
                        id = dialogId.toString(),
                        title = dialogRow[DialogTable.title],
                        icon = dialogRow[DialogTable.icon],
                        iconBg = dialogRow[DialogTable.iconBackground],
                        progress = ExampleProgress("roles:1", "subjects:2", "flows:3"),
                        description = dialogRow[DialogTable.description],
                        roles = listOf(roleA, roleB),
                        variables = null,
                        flowsSelection = null,
                        flows = flows
                    )
                }
            }

            val response = ExampleData(dialogsList)
            call.respondText(Json.encodeToString(response))
        }
    }
}

fun translate(lang: String, text: String): String {
    val hash = toHash(text)
    return transaction {
        TranslationTable.selectAll().where {
            (TranslationTable.hashEn eq hash) and (TranslationTable.lang eq lang)
        }.firstOrNull()?.get(TranslationTable.translated)
    } ?: throw Exception("Translation not found: $lang, \"$text\"")

}