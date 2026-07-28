import com.donik1998.cubeclash.buildlogic.configureKotlinJvm
import com.donik1998.cubeclash.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Pure-Kotlin modules — no Android SDK on the classpath at all. The domain layer
 * lives here so that "does this compile without Android?" is a compiler-enforced
 * question rather than a code-review one.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        configureKotlinJvm()

        dependencies {
            add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())
            add("testImplementation", libs.findLibrary("junit4").get())
            add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
        }
    }
}
