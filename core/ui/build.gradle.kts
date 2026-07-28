plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.library.compose)
}

android {
    namespace = "com.donik1998.cubeclash.core.ui"
}

dependencies {
    api(projects.core.model)
    api(projects.core.designsystem)

    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
