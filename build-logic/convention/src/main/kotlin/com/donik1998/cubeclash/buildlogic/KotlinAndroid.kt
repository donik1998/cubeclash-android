package com.donik1998.cubeclash.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** Everything every Android module in this repo agrees on. */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // AGP 9's generic `CommonExtension` exposes these as properties; the block form only exists
    // on the concrete Application/Library extensions, which a shared helper cannot see.
    commonExtension.apply {
        compileSdk = CubeClashSdk.COMPILE
        compileSdkMinor = CubeClashSdk.COMPILE_MINOR
        defaultConfig.minSdk = CubeClashSdk.MIN
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }

    configureKotlinJvm()
}

/** Shared Kotlin compiler configuration for both Android and pure-JVM modules. */
internal fun Project.configureKotlinJvm() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(
                providers.gradleProperty("cubeclash.warningsAsErrors").map(String::toBoolean).orElse(false),
            )
            freeCompilerArgs.addAll(
                // Opt-ins used across the codebase; declaring them centrally keeps
                // per-file @OptIn noise out of the domain layer.
                "-opt-in=kotlin.RequiresOptIn",
                "-Xannotation-default-target=param-property",
            )
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnit()
    }
}
