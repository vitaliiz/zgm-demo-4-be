package com.example

import kotlinx.serialization.Serializable

@Serializable
data class SourceData(
    val dialog: Dialog
)

@Serializable
data class Variables(
    val name: String,
    val values: List<VariableValue>
)

@Serializable
data class VariableValue(
    val variableId: Int,
    val text: String
)

@Serializable
data class SequenceItem(
    val seq: Int,
    val role: String,
    val multiText: List<MultiTextItem>
)

@Serializable
data class MultiTextItem(
    val variableId: Int? = null,
    val text: String
)

@Serializable
data class FlowsSelection(
    val row0: FlowSelectionRow0,
    val row1: FlowSelectionRow1
)

@Serializable
data class FlowSelectionRow0(
    val role: String,
    val text: String
)

@Serializable
data class FlowSelectionRow1(
    val role: String,
    val flowOptions: List<FlowSelectionOption>
)

@Serializable
data class FlowSelectionOption(
    val flowId: Int,
    val text: String
)

@Serializable
data class Flow(
    val id: Int,
    val sentences: List<SequenceItem>
)


@Serializable
data class Dialog(
    val title: String,
    val icon: String,
    val iconBg: String,
    val description: String,
    val roles: List<String>,
    val variables: Variables,
    val sequences: List<SequenceItem>,
    val flowsSelection: FlowsSelection,
    val flows: List<Flow>
)
