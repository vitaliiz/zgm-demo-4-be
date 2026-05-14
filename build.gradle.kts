val ktor_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(ktorLibs.plugins.ktor)
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(libs.logback.classic)

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.postgresql:postgresql:42.7.1")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}
