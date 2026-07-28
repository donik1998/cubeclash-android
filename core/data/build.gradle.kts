plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
}

android {
    namespace = "com.donik1998.cubeclash.core.data"

    buildFeatures.buildConfig = true

    defaultConfig {
        // Mirrors the Flutter client's `--dart-define=USE_FAKE_DATA`: the whole app runs on
        // in-memory fakes so it stays demoable while the backend is still being built.
        buildConfigField(
            "Boolean",
            "USE_FAKE_DATA",
            providers.gradleProperty("cubeclash.useFakeData").getOrElse("true"),
        )
    }
}

dependencies {
    api(projects.core.domain)
    api(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
}
