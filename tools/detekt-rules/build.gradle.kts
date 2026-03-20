plugins {
    id("java-library")
    kotlin("jvm")
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.gitlab.detekt.api)
}
