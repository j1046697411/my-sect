import shadow.bundletool.com.android.tools.r8.internal.li

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.dokka) apply false
}

// 子项目都有的内容需要统一配置在这个下面
subprojects {
    if (project.path.startsWith(":business:")) {
        apply(plugin = "org.jetbrains.kotlin.multiplatform")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }
}
