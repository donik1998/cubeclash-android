plugins {
    alias(libs.plugins.cubeclash.android.library)
    alias(libs.plugins.cubeclash.android.hilt)
}

android {
    namespace = "com.donik1998.cubeclash.core.database"
}

ksp {
    // Committed so a schema change shows up in review as a diff, not as a surprise migration.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    api(libs.room.runtime)
    api(libs.room.ktx)
    ksp(libs.room.compiler)
}
