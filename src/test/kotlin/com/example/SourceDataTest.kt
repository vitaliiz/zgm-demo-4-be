package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SourceDataTest {

    @Test
    fun `should deserialize source yaml correctly`() {
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

        // --- Assertions ---

        // Verify roles
        assertNotNull(parsedData.dialog.roles)
        assertEquals(2, parsedData.dialog.roles.size)
        assertEquals("Customer", parsedData.dialog.roles[0])
        assertEquals("Waiter", parsedData.dialog.roles[1])

        // Verify variables
        assertEquals("subject", parsedData.dialog.variables.name)
        assertEquals(2, parsedData.dialog.variables.values.size)
        assertEquals("Menu", parsedData.dialog.variables.values[0].text)

        // Verify sequences (3 sequences defined in the 'sequences' section)
        assertEquals(3, parsedData.dialog.sequences.size)
        assertEquals(0, parsedData.dialog.sequences[0].seq)
        assertEquals("Customer", parsedData.dialog.sequences[0].role)

        // Verify dialog flowsSelection
        val row0 = parsedData.dialog.flowsSelection.row0
        assertNotNull(row0)
        assertEquals("Customer", row0.role)
        assertEquals("Can I have a menu please ?", row0.text)

        val row1 = parsedData.dialog.flowsSelection.row1
        assertNotNull(row1)
        assertEquals("Waiter", row1.role)
        assertNotNull(row1.flowOptions)
        assertEquals(2, row1.flowOptions.size)
        assertEquals(0, row1.flowOptions[0].flowId)

        // Verify flows (deserialization of aliases points to the same object structure)
        assertEquals(2, parsedData.dialog.flows.size)
        assertEquals(0, parsedData.dialog.flows[0].id)
        assertEquals(2, parsedData.dialog.flows[0].sentences.size)
        assertEquals("Customer", parsedData.dialog.flows[0].sentences[0].role)
        assertEquals("Waiter", parsedData.dialog.flows[0].sentences[1].role)
        
        println("Deserialization successful!")

        // 3. Serialize to JSON and print to stdout
        val jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedData)
        println("--- Serialized JSON ---")
        println(jsonString)
        println("-----------------------")
    }
}
