plugins {
    alias(libs.plugins.cubeclash.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(libs.javax.inject)
}
