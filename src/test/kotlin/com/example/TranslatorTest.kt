package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TranslatorTest {

    @Test
    fun `should translate all text`() {
        // Read the yaml file from the project root
        val yamlFile = File("data/source.yaml")
        assertTrue(yamlFile.exists(), "The source.yaml file should exist at ${yamlFile.absolutePath}")

        // 1. Use SnakeYAML to load the YAML file into a Map.
        // SnakeYAML is the industry standard for Java and handles YAML anchors/aliases correctly.
        val yamlLoader = Yaml()
        val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())

        // 2. Use Jackson to convert the Map into our Kotlin Data Classes.
        // jackson-module-kotlin ensures seamless mapping to Kotlin's data classes.
        val mapper = ObjectMapper().registerKotlinModule()
        val parsedData = mapper.convertValue(rawMap, SourceData::class.java)

        val translationContextEN : String = parsedData.dialog.sequences
            .flatMap { it.multiText }
            .filter { it.variableId == null || it.variableId == 0 }
            .joinToString("\\n") { it.text }

        parsedData.dialog.variables.values.forEach { translate("NL", it.text, translationContextEN) }

        parsedData.dialog.sequences
            .flatMap { it.multiText }
            .map { it.text }
            .forEach { textEN ->
                translate("NL", textEN, translationContextEN)
            }
    }
}
