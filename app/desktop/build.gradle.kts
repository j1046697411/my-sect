import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.roborazzi)
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

        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":business:mvi"))
                implementation(project(":business:domain"))
                implementation(project(":business:engine"))
                implementation(project(":business:data"))
                implementation(project(":business:goap"))
                implementation(project(":business:goap-framework"))
                implementation(project(":business:presentation"))
                implementation(project(":business:feature-game"))
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation("io.github.takahirom.roborazzi:roborazzi-compose-desktop:${libs.versions.roborazzi.get()}") {
                    exclude(group = "org.jetbrains.compose.ui", module = "ui-test-junit4-desktop")
                }
                implementation(compose.desktop.uiTestJUnit4)
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
