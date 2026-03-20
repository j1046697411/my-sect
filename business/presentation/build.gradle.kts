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
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.compose.material3)
                implementation(libs.flowmvi.compose)
            }
        }
        val commonTest by getting { }
        val desktopMain by getting { }
    }
}
