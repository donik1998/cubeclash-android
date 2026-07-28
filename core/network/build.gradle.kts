plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
    alias(libs.plugins.kotlin.serialization)
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
}
