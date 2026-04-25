package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExampleData(
    val dialogs: List<ExampleDialog>
)

@Serializable
data class ExampleDialog(
    val id: String,
    val title: String,
    val icon: String,
    val iconBg: String,
    val progress: ExampleProgress,
    val language: String,
    val totalProgress: String,
    val description: String,
    val roles: List<String>,
    val variables: ExampleVariables,
    val flowsSelection: List<ExampleFlowSelectionItem>,
    val flows: List<ExampleFlow>
)

@Serializable
data class ExampleProgress(
    val roles: String,
    val subjects: String,
    val flows: String
)

@Serializable
data class ExampleVariables(
    val name: String,
    val values: List<ExampleVariableValue>
)

@Serializable
data class ExampleVariableValue(
    @SerialName("variable_id")
    val variableId: Int,
    val text: ExampleTextWithAudio
)

@Serializable
data class ExampleTextWithAudio(
    val native: String,
    val foreign: String,
    val audioUrl: String
)

@Serializable
data class ExampleFlowSelectionItem(
    val row: Int,
    val role: String,
    val text: List<ExampleFlowSelectionText>
)

@Serializable
data class ExampleFlowSelectionText(
    @SerialName("flow_id")
    val flowId: Int? = null,
    val native: String,
    val foreign: String
)

@Serializable
data class ExampleFlow(
    val id: Int,
    val sentences: List<ExampleSentence>
)

@Serializable
data class ExampleSentence(
    val seq: Int,
    val role: String,
    val text: List<ExampleSentenceText>
)

@Serializable
data class ExampleSentenceText(
    @SerialName("variable_id")
    val variableId: Int? = null,
    val native: String,
    val foreign: String,
    val audioUrl: String
)
