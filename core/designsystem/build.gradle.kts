plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.library.compose)
}

android {
    namespace = "com.donik1998.cubeclash.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
}
