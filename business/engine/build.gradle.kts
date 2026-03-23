import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlinMultiplatform)
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
                implementation(project(":business:goap-framework"))
                implementation(project(":business:goap"))
                implementation(project(":business:domain"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":business:goap"))
                implementation(project(":business:domain"))
                implementation(libs.kotlin.test)
            }
        }
        val desktopMain by getting { }
    }
}
