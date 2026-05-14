package com.example

import org.example.DialogFlowStepSentenceTable
import org.example.DialogTable
import org.example.RoleTable
import org.example.SentenceTable
import org.example.TranslationTable
import org.example.WordTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
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

            val roleA = getRole(rolesMap,dialogRow[DialogTable.roleAId].value)
            val roleB = getRole(rolesMap,dialogRow[DialogTable.roleBId].value)

            // Fetch flows for this dialog
            val flowRecords = (DialogFlowStepSentenceTable innerJoin SentenceTable innerJoin WordTable)
                .selectAll()
                .where { DialogFlowStepSentenceTable.dialogId eq dialogId }
                .orderBy(DialogFlowStepSentenceTable.flowId to SortOrder.ASC, DialogFlowStepSentenceTable.step to SortOrder.ASC)
                .toList()

            val variablesList = flowRecords.distinctBy { it[DialogFlowStepSentenceTable.wordId] }.map { row ->
                val wordText = row[WordTable.word]
                val wordId = row[DialogFlowStepSentenceTable.wordId].value
                ExampleVariableValue(
                    variableId = wordId,
                    text = ExampleTextWithAudio(
                        native = wordText,
                        foreign = getTranslation("NL", wordText),
                        audioUrl = "http://192.168.2.74:8080/media/voice/NL/${toHash(wordText)}"
                    )
                )
            }
            val variables = if (variablesList.isNotEmpty()) ExampleVariables(name = "subject", values = variablesList) else null

            val flows = flowRecords.groupBy { it[DialogFlowStepSentenceTable.flowId] }.map { (flowId, flowRows) ->
                val sentences = flowRows.groupBy { it[DialogFlowStepSentenceTable.step] to it[DialogFlowStepSentenceTable.roleId] }
                    .map { (key, stepRoleRows) ->
                        val (step, roleId) = key
                        ExampleSentence(
                            seq = step,
                            role = getRole(rolesMap,roleId.value),
                            text = stepRoleRows.map { row ->
                                val nativeText = row[SentenceTable.text]
                                ExampleSentenceText(
                                    variableId = row[DialogFlowStepSentenceTable.wordId].value,
                                    native = nativeText,
                                    foreign = getTranslation("NL", nativeText),
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
                variables = variables,
                flowsSelection = null,
                flows = flows
            )
        }
    }

    return dialogsList
}


private fun getRole(rolesMap:  Map<Int, String>, roleId: Int): String =
    rolesMap[roleId] ?: throw Exception("role_id ${roleId} not found")


private fun getTranslation(lang: String, text: String): String {
    val hash = toHash(text)
    return transaction {
        TranslationTable.selectAll().where {
            (TranslationTable.hashEn eq hash) and (TranslationTable.lang eq lang)
        }.firstOrNull()?.get(TranslationTable.translated)
    } ?: throw Exception("Translation not found: $lang, \"$text\"")
}