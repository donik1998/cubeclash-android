plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
}

android {
    namespace = "com.donik1998.cubeclash.core.datastore"
}

dependencies {
    api(projects.core.domain)
    implementation(libs.androidx.datastore.preferences)
}
