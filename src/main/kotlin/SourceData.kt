package com.example

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceData(
    val roles: List<String>,
    val variables: Variables,
    val sequences: List<SequenceItem>,
    val dialog: Dialog
)

@Serializable
data class Variables(
    val name: String,
    val values: List<VariableValue>
)

@Serializable
data class VariableValue(
    @JsonProperty("variable_id")
    @SerialName("variable_id")
    val variable_id: Int,
    val text: String
)

@Serializable
data class SequenceItem(
    val seq: Int,
    val role: String,
    @JsonProperty("multiText")
    @SerialName("multiText")
    val multiText: List<MultiTextItem>
)

@Serializable
data class MultiTextItem(
    @JsonProperty("variable_id")
    @SerialName("variable_id")
    val variable_id: Int? = null,
    val text: String
)

@Serializable
data class Dialog(
    val title: String,
    val icon: String,
    val iconBg: String,
    val description: String,
    @JsonProperty("flowsSelection")
    @SerialName("flowsSelection")
    val flowsSelection: FlowsSelection,
    val flows: List<Flow>
)

@Serializable
data class FlowsSelection(
    @JsonProperty("row_0")
    @SerialName("row_0")
    val row_0: FlowSelectionRow,
    @JsonProperty("row_1")
    @SerialName("row_1")
    val row_1: FlowSelectionRow
)

@Serializable
data class FlowSelectionRow(
    val role: String,
    val text: String? = null,
    @JsonProperty("flowOptions")
    @SerialName("flowOptions")
    val flowOptions: List<FlowOption>? = null
)

@Serializable
data class FlowOption(
    @JsonProperty("flow_id")
    @SerialName("flow_id")
    val flow_id: Int,
    val text: String
)

@Serializable
data class Flow(
    val id: Int,
    val sentences: List<SequenceItem>
)
