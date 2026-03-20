import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
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

compose.desktop {
    application {
        mainClass = "com.sect.game.client.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SectMvp"
            packageVersion = "1.0.0"
        }
    }
}
