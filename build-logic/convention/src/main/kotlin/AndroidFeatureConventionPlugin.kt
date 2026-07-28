import com.android.build.api.dsl.LibraryExtension
import com.donik1998.cubeclash.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * A feature module is a UI slice: it owns screens, view models and its own nav graph,
 * and it may only see the domain + design system — never another feature, and never
 * the data layer directly.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("cubeclash.android.library")
            apply("cubeclash.android.library.compose")
            apply("cubeclash.android.hilt")
        }

        extensions.configure<LibraryExtension> {
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:ui"))

            add("implementation", libs.findLibrary("androidx.core.ktx").get())
            add("implementation", libs.findLibrary("androidx.activity.compose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
            add("implementation", libs.findLibrary("androidx.navigation.compose").get())
            add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
            add("implementation", libs.findLibrary("kotlinx.serialization.json").get())

            add("testImplementation", project(":core:testing"))
            add("androidTestImplementation", libs.findLibrary("androidx.test.ext.junit").get())
        }

        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
    }
}
