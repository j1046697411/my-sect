
plugins {
    alias(libs.plugins.kotlinx.compose)
    alias(libs.plugins.kotlinx.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

kotlin {
    jvm()
    androidTarget()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(compose.material3)
                implementation(libs.flowmvi.core)
                implementation(libs.kodein)
                implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
            resources.srcDirs("src/jvmMain/resources")
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.activity.ktx)
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}


android {
    namespace = "com.sect.game.client"
    compileSdk = libs.versions.android.target.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "**/*.kotlin_builtins"
        }
    }
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(true)
            skipDeprecated.set(true)
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(file("detekt.yml"))
    source.setFrom(files("src/commonMain/kotlin", "src/commonTest/kotlin"))
}

tasks.register<Copy>("copyDetektServices") {
    from(file("src/jvmMain/resources/META-INF/services"))
    into(layout.buildDirectory.dir("classes/kotlin/jvm/main/META-INF/services"))
    dependsOn("compileKotlinJvm")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    dependsOn("copyDetektServices")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    dependsOn(tasks.named("compileKotlinJvm"))
}