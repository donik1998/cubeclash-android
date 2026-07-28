import com.android.build.api.dsl.LibraryExtension
import com.donik1998.cubeclash.buildlogic.configureKotlinAndroid
import com.donik1998.cubeclash.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Kotlin comes with AGP 9 — see AndroidApplicationConventionPlugin.
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
            testOptions.unitTests.isReturnDefaultValues = true
        }

        dependencies {
            add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
            add("testImplementation", libs.findLibrary("junit4").get())
            add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
        }
    }
}
