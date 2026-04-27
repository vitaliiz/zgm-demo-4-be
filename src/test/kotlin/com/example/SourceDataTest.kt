package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.test.Test

class SourceDataTest {
    val yamlLoader = Yaml()
    val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should deserialize source yaml correctly`() {
        val yamlFile = File("data/source.yaml")
        val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())
        mapper.convertValue(rawMap, SourceData::class.java)
    }

    @Test
    fun parse_no_flows() {
        val yamlFile = File("data/source1.yaml")
        val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())
        mapper.convertValue(rawMap, SourceData::class.java)
    }

    @Test
    fun parse_no_flows_no_variables() {
        val yamlFile = File("data/source2.yaml")
        val rawMap: Map<String, Any> = yamlLoader.load(yamlFile.inputStream())
        mapper.convertValue(rawMap, SourceData::class.java)
    }
}
