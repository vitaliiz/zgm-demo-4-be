package com.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object WordTable : IntIdTable("word") {
    val word = text("word").uniqueIndex()
}

object RoleTable : IntIdTable("role") {
    val wordId = reference("word_id", WordTable).uniqueIndex()
}

object SentenceTable : IntIdTable("sentence") {
    val text = text("text").uniqueIndex()
}

object DialogTable : IntIdTable("dialog") {
    val title = text("title")
    val description = text("description")
    val icon = text("icon")
    val iconBackground = text("icon_background")
    val roleAId = reference("role_a_id", RoleTable)
    val roleBId = reference("role_b_id", RoleTable)
}

object DialogFlowStepSentenceTable : Table("dialog_flow_step_sentence") {
    val dialogId = reference("dialog_id", DialogTable)
    val flowId = integer("flow_id")
    val step = integer("step")
    val wordId = reference("word_id", WordTable)
    val sentenceId = reference("sentence_id", SentenceTable)
    val roleId = reference("role_id", RoleTable)
    override val primaryKey = PrimaryKey(dialogId, flowId, step, sentenceId, roleId)
}

object DialogFlowSelectionTable : Table("dialog_flow_selection") {
    val dialogId = reference("dialog_id", DialogTable)
    val row0Role = reference("row_0_role", RoleTable)
    val row1Role = reference("row_1_role", RoleTable)
    val row0Text = text("row_0_text")
    val row1Flow0Text = text("row_1_flow_0_text")
    val row1Flow1Text = text("row_1_flow_1_text")
}

object TranslationTable : IntIdTable("translation") {
    val hashEn = varchar("hash_en", 100)
    val lang = varchar("lang", 10)
    val translated = text("translated")
}

object VoiceTable : IntIdTable("voice") {
    val hashEn = varchar("hash_en", 100)
    val lang = varchar("lang", 10)
    val data = blob("data")
}
