package com.example

import org.example.DialogFlowStepSentenceTable
import org.example.DialogTable
import org.example.RoleTable
import org.example.SentenceTable
import org.example.TranslationTable
import org.example.WordTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.component1
import kotlin.collections.component2

fun buildDialogList(): List<ExampleDialog> {
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

    return dialogsList
}


fun translate(lang: String, text: String): String {
    val hash = toHash(text)
    return transaction {
        TranslationTable.selectAll().where {
            (TranslationTable.hashEn eq hash) and (TranslationTable.lang eq lang)
        }.firstOrNull()?.get(TranslationTable.translated)
    } ?: throw Exception("Translation not found: $lang, \"$text\"")
}