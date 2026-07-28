package com.donik1998.cubeclash.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.findLibrary("androidx.compose.bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.findLibrary("androidx.compose.ui").get())
        add("implementation", libs.findLibrary("androidx.compose.ui.graphics").get())
        add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
        add("implementation", libs.findLibrary("androidx.compose.material3").get())

        add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
        add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())
        add("androidTestImplementation", libs.findLibrary("androidx.compose.ui.test.junit4").get())
    }
}

internal fun DependencyHandler.implementation(dependency: Any) = add("implementation", dependency)
