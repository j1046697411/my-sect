plugins {
    alias(libs.plugins.dokka)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":business:domain"))
                implementation(libs.flowmvi.core)
            }
        }
        val commonTest by getting { }
        val desktopMain by getting { }
    }
}
