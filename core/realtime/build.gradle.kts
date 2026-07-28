plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donik1998.cubeclash.core.realtime"

    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField(
            "Boolean",
            "USE_FAKE_DATA",
            providers.gradleProperty("cubeclash.useFakeData").getOrElse("true"),
        )
    }
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.socket.io.client)
    implementation(libs.kotlinx.serialization.json)
}
