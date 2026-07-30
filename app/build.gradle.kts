import java.util.Properties

plugins {
    alias(libs.plugins.cubeclash.android.application)
    alias(libs.plugins.cubeclash.android.application.compose)
    alias(libs.plugins.cubeclash.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing credentials, read from `key.properties` at the repo root.
//
// That file is gitignored and holds the upload keystore's path and passwords —
// it is deliberately NOT in version control and never will be. When it is
// absent (CI, a fresh clone, a contributor without the key) the release build
// falls back to debug signing so `assembleRelease` still compiles and is
// verifiable, which is what the original placeholder was protecting. The only
// thing that changes without the file is that the artefact is unshippable,
// which is correct: an unsigned-by-the-real-key build should never reach a store.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("key.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "com.donik1998.cubeclash"

    defaultConfig {
        applicationId = "com.donik1998.cubeclash"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // So a debug build can sit next to the Flutter client on the same device.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.datastore)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.realtime)

    implementation(projects.feature.auth)
    implementation(projects.feature.timer)
    implementation(projects.feature.race)
    implementation(projects.feature.stats)
    implementation(projects.feature.profile)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
}
