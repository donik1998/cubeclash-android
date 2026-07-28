plugins {
    alias(libs.plugins.cubeclash.android.library)
}

android {
    namespace = "com.donik1998.cubeclash.core.testing"
}

dependencies {
    api(projects.core.model)
    api(projects.core.domain)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
}
