plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":business:domain"))
                implementation(project(":business:mvi"))
                implementation(project(":business:engine"))
                implementation(project(":business:presentation"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.flowmvi.core)
                implementation(libs.flowmvi.compose)
                implementation(libs.compose.material3)
            }
        }
        val commonTest by getting { }
        val desktopMain by getting { }
    }
}
