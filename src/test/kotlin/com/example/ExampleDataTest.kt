package com.example

import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExampleDataTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `test deserialization of example json`() {
        val jsonFile = File("data/example.json")
        val jsonContent = jsonFile.readText()
        val root = json.decodeFromString<ExampleData>(jsonContent)

        assertNotNull(root)
        assertEquals(1, root.dialogs.size)
        
        val dialog = root.dialogs[0]
        assertEquals("0", dialog.id)
        assertEquals("Ordering food", dialog.title)
        assertEquals(2, dialog.roles.size)
        assertEquals("subject", dialog.variables.name)
        assertEquals(2, dialog.flowsSelection.size)
        assertEquals(1, dialog.flows.size)
        
        // Verify nested fields
        assertEquals(0, dialog.variables.values[0].variableId)
        assertEquals("Menu", dialog.variables.values[0].text.native)
        
        assertEquals("Customer", dialog.flowsSelection[0].role)
        assertEquals(1, dialog.flowsSelection[0].text.size)
        
        assertEquals("Waiter", dialog.flowsSelection[1].role)
        assertEquals(2, dialog.flowsSelection[1].text.size)
        assertEquals(0, dialog.flowsSelection[1].text[0].flowId)
        
        assertEquals(0, dialog.flows[0].id)
        assertEquals(2, dialog.flows[0].sentences.size)
        assertEquals(0, dialog.flows[0].sentences[0].text[0].variableId)
    }
}
