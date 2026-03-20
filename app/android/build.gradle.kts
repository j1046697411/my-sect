plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.sect.game.app.android"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
}

android {
    sourceSets {
        named("main") {
            dependencies {
                implementation(project(":business:data"))
                implementation(libs.androidx.activity.compose)
                implementation(libs.compose.material3)
            }
        }
    }
}
