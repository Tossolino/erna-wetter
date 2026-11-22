plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// App-Version aus android/VERSION (MAJOR.MINOR.PATCH)
val appVersion = rootProject.file("VERSION").readText().trim()
val (vMaj, vMin, vPat) = appVersion.split(".").map { it.toInt() }

android {
    namespace = "de.tossolino.erna"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.tossolino.erna"
        minSdk = 26
        targetSdk = 35
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
    // Upload-Schlüssel für den Play Store (nur in GitHub Actions vorhanden, als geheime Werte)
    val uploadKeystore = System.getenv("ERNA_UPLOAD_KEYSTORE")
    val uploadPassword = System.getenv("ERNA_UPLOAD_PASSWORD")
    if (uploadKeystore != null && uploadPassword != null) {
        signingConfigs.create("upload") {
            storeFile = file(uploadKeystore)
            storePassword = uploadPassword
            keyAlias = "upload"
            keyPassword = uploadPassword
        }
    }
    buildTypes {
        getByName("debug") { signingConfig = signingConfigs.getByName("debug") }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("upload")
        }
    }

    // Zwei Vertriebswege aus demselben Code:
    //  github: Download über GitHub, mit eingebautem Updater
    //  play:   Google Play (Updates macht der Play Store; Selbst-Update ist dort verboten)
    flavorDimensions += "vertrieb"
    productFlavors {
        create("github") {
            dimension = "vertrieb"
            buildConfigField("boolean", "SELF_UPDATE", "true")
        }
        create("play") {
            dimension = "vertrieb"
            buildConfigField("boolean", "SELF_UPDATE", "false")
        }
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
