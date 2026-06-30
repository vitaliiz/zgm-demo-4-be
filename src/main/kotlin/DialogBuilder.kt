package com.example

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun buildDialogList(): List<ExampleDialog> = transaction {
    val rolesMap = (RoleTable innerJoin WordTable).selectAll().associate {
        it[RoleTable.id].value to it[WordTable.word]
    }

    DialogTable.selectAll().map { dialogRow ->
        val dialogId = dialogRow[DialogTable.id].value
        val title = dialogRow[DialogTable.title]
        val description = dialogRow[DialogTable.description]
        val icon = dialogRow[DialogTable.icon]
        val iconBg = dialogRow[DialogTable.iconBackground]
        val roleAId = dialogRow[DialogTable.roleAId].value
        val roleBId = dialogRow[DialogTable.roleBId].value

        val roleA = getRole(rolesMap, roleAId)
        val roleB = getRole(rolesMap, roleBId)

        // Fetch alternatives for variables
        val alternativesRows = (DialogAlternativeTable innerJoin AlternativeTable innerJoin WordTable innerJoin SentenceTable)
            .selectAll()
            .where { DialogAlternativeTable.dialogId eq dialogId }
            .toList()

        val variablesList = alternativesRows
            .distinctBy { it[AlternativeTable.wordId] }
            .map { row ->
                val wordText = row[WordTable.word]
                val wordId = row[AlternativeTable.wordId].value
                ExampleVariableValue(
                    variableId = wordId,
                    text = ExampleTextWithAudio(
                        native = wordText,
                        foreign = getTranslation("NL", wordText),
                        audioUrl = "http://167.99.215.169:8080/media/voice/NL/${toHash(wordText)}"
                    )
                )
            }
        val variables = if (variablesList.isNotEmpty()) ExampleVariables(name = "subject", values = variablesList) else null

        // Fetch base sentences
        val baseSentencesRows = (DialogFlowStepSentenceTable innerJoin SentenceTable)
            .selectAll()
            .where { DialogFlowStepSentenceTable.dialogId eq dialogId }
            .toList()

        // Combine all sentences for flows
        val allSentences = baseSentencesRows.map { row ->
            SentenceInfo(
                flowId = row[DialogFlowStepSentenceTable.flowId],
                step = row[DialogFlowStepSentenceTable.step],
                roleId = row[DialogFlowStepSentenceTable.roleId].value,
                sentenceText = row[SentenceTable.text],
                variableId = null
            )
        } + alternativesRows.map { row ->
            SentenceInfo(
                flowId = row[AlternativeTable.flowId],
                step = row[AlternativeTable.step],
                roleId = row[AlternativeTable.roleId].value,
                sentenceText = row[SentenceTable.text],
                variableId = row[AlternativeTable.wordId].value
            )
        }

        val flows = allSentences.groupBy { it.flowId }.map { (flowId, flowSentences) ->
            val sentences = flowSentences.groupBy { it.step }.map { (step, stepSentences) ->
                val roleId = stepSentences.first().roleId
                val hasAlternatives = stepSentences.any { it.variableId != null }
                val targetSentences = if (hasAlternatives) stepSentences.filter { it.variableId != null } else stepSentences

                ExampleSentence(
                    seq = step,
                    role = getRole(rolesMap, roleId),
                    text = targetSentences.map { s ->
                        ExampleSentenceText(
                            variableId = s.variableId,
                            native = s.sentenceText,
                            foreign = getTranslation("NL", s.sentenceText),
                            audioUrl = "http://167.99.215.169:8080/media/voice/NL/${toHash(s.sentenceText)}"
                        )
                    }
                )
            }.sortedBy { it.seq }
            ExampleFlow(id = flowId, sentences = sentences)
        }.sortedBy { it.id }

        val flowsSelection = DialogFlowSelectionTable.selectAll().where { DialogFlowSelectionTable.dialogId eq dialogId }
            .firstOrNull()?.let { selectionRow ->
                listOf(
                    ExampleFlowSelectionItem(
                        row = 0,
                        role = getRole(rolesMap, selectionRow[DialogFlowSelectionTable.row0Role].value),
                        text = listOf(
                            ExampleFlowSelectionText(
                                flowId = null,
                                native = selectionRow[DialogFlowSelectionTable.row0Text],
                                foreign = getTranslation("NL", selectionRow[DialogFlowSelectionTable.row0Text])
                            )
                        )
                    ),
                    ExampleFlowSelectionItem(
                        row = 1,
                        role = getRole(rolesMap, selectionRow[DialogFlowSelectionTable.row1Role].value),
                        text = listOf(
                            ExampleFlowSelectionText(
                                flowId = 1,
                                native = selectionRow[DialogFlowSelectionTable.row1Flow0Text],
                                foreign = getTranslation("NL", selectionRow[DialogFlowSelectionTable.row1Flow0Text])
                            ),
                            ExampleFlowSelectionText(
                                flowId = 2,
                                native = selectionRow[DialogFlowSelectionTable.row1Flow1Text],
                                foreign = getTranslation("NL", selectionRow[DialogFlowSelectionTable.row1Flow1Text])
                            )
                        )
                    )
                )
            }

        ExampleDialog(
            id = dialogId.toString(),
            title = title,
            icon = icon,
            iconBg = iconBg,
            progress = ExampleProgress("roles:1", "subjects:2", "flows:3"),
            description = description,
            roles = listOf(roleA, roleB),
            variables = variables,
            flowsSelection = flowsSelection,
            flows = flows
        )
    }
}

private data class SentenceInfo(
    val flowId: Int,
    val step: Int,
    val roleId: Int,
    val sentenceText: String,
    val variableId: Int?
)

private fun getRole(rolesMap: Map<Int, String>, roleId: Int): String =
    rolesMap[roleId] ?: throw Exception("role_id $roleId not found")

private fun getTranslation(lang: String, text: String): String {
    val hash = toHash(text)
    return transaction {
        TranslationTable.selectAll().where {
            (TranslationTable.hashEn eq hash) and (TranslationTable.lang eq lang)
        }.firstOrNull()?.get(TranslationTable.translated)
    } ?: throw Exception("Translation not found: $lang, \"$text\"")
}
