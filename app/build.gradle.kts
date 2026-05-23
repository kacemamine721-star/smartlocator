plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.project_mobile"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.project_mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("x86_64")
            abiFilters.add("x86")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("projectDebug") {
            storeFile = file("../debug-local.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("projectRelease") {
            storeFile = file("../debug-local.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("projectDebug")
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/api/\"")
        }

        release {
            signingConfig = signingConfigs.getByName("projectRelease")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://smartlocator-api.onrender.com/api/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        // Suppress API level warnings for theme attributes
        // windowLightNavigationBar requires API 27, but we support API 24+
        // This attribute gracefully degrades on older devices
        disable.add("NewApi")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.maplibre.android)
    implementation(libs.maplibre.annotation)
    
    // Explicit coordinatorlayout to resolve duplicate class conflicts
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Room — local database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Lifecycle — ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Retrofit + OkHttp — REST API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson — JSON parsing (GeoJSON loader)
    implementation("com.google.code.gson:gson:2.10.1")

    // Google Location Services — GPS / FusedLocationProvider
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
