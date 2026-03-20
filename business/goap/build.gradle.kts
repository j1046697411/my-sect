plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":business:goap-framework"))
                implementation(project(":business:domain"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val desktopMain by getting { }
    }
}
