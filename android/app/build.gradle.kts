plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// App-Version aus android/VERSION (MAJOR.MINOR.PATCH)
val appVersion = rootProject.file("VERSION").readText().trim()
val (vMaj, vMin, vPat) = appVersion.split(".").map { it.toInt() }

android {
    namespace = "de.tossolino.erna"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.tossolino.erna"
        minSdk = 26
        targetSdk = 34
        versionCode = vMaj * 10000 + vMin * 100 + vPat
        versionName = appVersion
    }

    signingConfigs {
        // Fester Schlüssel aus dem Repository: alle Versionen gleich signiert -> Updates installierbar
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") { signingConfig = signingConfigs.getByName("debug") }
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
