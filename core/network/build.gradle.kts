import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Unit tests run in a forked JVM that does NOT inherit the Gradle daemon's -D properties, so the
// live-API gate (LiveApiTest / LiveWireTest read `cubeclash.liveApi` / `cubeclash.apiBaseUrl`) must
// be forwarded explicitly. Absent → the suites Assume-skip and the task stays green with no server.
tasks.withType<Test>().configureEach {
    listOf("cubeclash.liveApi", "cubeclash.apiBaseUrl").forEach { key ->
        (findProperty(key) as String?)?.let { systemProperty(key, it) }
        System.getProperty(key)?.let { systemProperty(key, it) }
    }
}

android {
    namespace = "com.donik1998.cubeclash.core.network"

    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${providers.gradleProperty("cubeclash.apiBaseUrl").getOrElse("http://10.0.2.2:3000/v1/")}\"",
        )
        buildConfigField(
            "String",
            "SOCKET_URL",
            "\"${providers.gradleProperty("cubeclash.socketUrl").getOrElse("http://10.0.2.2:3000")}\"",
        )
    }
}

dependencies {
    api(projects.core.domain)
    api(projects.core.model)

    api(libs.retrofit.core)
    api(libs.okhttp.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    api(libs.kotlinx.serialization.json)

    // Live-API integration suite (LiveApiTest / LiveWireTest): the in-memory TokenStore lets the
    // real Retrofit/OkHttp stack run headless under a plain JVM unit test, with no DataStore.
    testImplementation(projects.core.testing)
}
