package com.donik1998.cubeclash.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** The single `libs` version catalog, reachable from inside a convention plugin. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** SDK levels live in one place so a bump is a one-line change across 16 modules. */
internal object CubeClashSdk {
    const val COMPILE = 37

    /** AGP 9 tracks SDK minor releases; the AndroidX 2026.06 BOM wants at least 37.1. */
    const val COMPILE_MINOR = 1
    const val TARGET = 37

    /**
     * API 26 (Android 8.0) is the floor: it is where `java.time` becomes available
     * without desugaring, which the WCA result model leans on heavily.
     */
    const val MIN = 26
}
