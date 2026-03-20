import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.sect.game.client"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    packaging {
        resources {
            excludes += "**/*.kotlin_builtins"
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
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

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":business:domain"))
                implementation(project(":business:engine"))
                implementation(project(":business:data"))
                implementation(project(":business:goap"))
                implementation(project(":business:goap-framework"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation(libs.flowmvi.compose)
            }
            resources.srcDirs("src/jvmMain/resources")
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.activity.compose)
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

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:" +
                "stabilityConfigurationPath=" +
                project.layout.projectDirectory.file(
                    "stability_definitions.txt",
                ).asFile.absolutePath,
        )
    }
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(true)
            skipDeprecated.set(true)
        }
        register("businessDomain") {
            sourceRoot(file("../business/domain/src/commonMain/kotlin"))
            displayName.set("domain")
        }
        register("businessEngine") {
            sourceRoot(file("../business/engine/src/commonMain/kotlin"))
            displayName.set("engine")
        }
        register("businessGoap") {
            sourceRoot(file("../business/goap/src/commonMain/kotlin"))
            displayName.set("goap")
        }
        register("businessGoapFramework") {
            sourceRoot(file("../business/goap-framework/src/commonMain/kotlin"))
            displayName.set("goap-framework")
        }
        register("businessMvi") {
            sourceRoot(file("../business/mvi/src/commonMain/kotlin"))
            displayName.set("mvi")
        }
        register("businessPresentation") {
            sourceRoot(file("../business/presentation/src/commonMain/kotlin"))
            displayName.set("presentation")
        }
        register("businessFeatureGame") {
            sourceRoot(file("../business/feature-game/src/commonMain/kotlin"))
            displayName.set("feature-game")
        }
        register("businessData") {
            sourceRoot(file("../business/data/src/commonMain/kotlin"))
            displayName.set("data")
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("../tools/detekt-rules/detekt.yml"))
    source.setFrom(files("src/commonMain/kotlin", "src/commonTest/kotlin"))
    dependencies {
        detektPlugins(project(":tools:detekt-rules"))
    }
}

ktlint {
    filter {
        exclude("**/main.kt")
        exclude("**/RealmTest.kt")
    }
}
