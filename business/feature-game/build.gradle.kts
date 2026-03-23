import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = libs.versions.kover.min.line.get().toInt()
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
                bound {
                    minValue = libs.versions.kover.min.branch.get().toInt()
                    coverageUnits = CoverageUnit.BRANCH
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
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
