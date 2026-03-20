import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    application
}

kotlin {
    sourceSets {
        main {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.compose.material3)
                implementation(libs.kodein)
                implementation(libs.flowmvi.core)
                implementation(libs.flowmvi.compose)
                implementation(project(":business:presentation"))
                implementation(project(":business:feature-game"))
            }
        }
    }
}

application {
    mainClass.set("com.sect.game.client.MainKt")
}
