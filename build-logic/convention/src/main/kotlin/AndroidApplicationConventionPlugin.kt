import com.android.build.api.dsl.ApplicationExtension
import com.donik1998.cubeclash.buildlogic.CubeClashSdk
import com.donik1998.cubeclash.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 has built-in Kotlin support: applying `org.jetbrains.kotlin.android` on top of it
        // is now an error, not a redundancy.
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = CubeClashSdk.TARGET
        }
    }
}
