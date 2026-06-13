plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.aistudio.opais.kgvxt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aistudio.opais.kgvxt"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("arm64-v8a"))
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true      
            isShrinkResources = true    
            isCrunchPngs = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            // Safely fetches the key from GitHub Secrets environment during build
            val apiKey = System.getenv("AIzaSyCFU9BvtAYQkxP5fM7mbhNWQEgCU-vuX4E") ?: "MOCK_KEY"
            buildConfigField("String", "AIzaSyCFU9BvtAYQkxP5fM7mbhNWQEgCU-vuX4E", "\"$apiKey\"")
        }
        debug {
            isMinifyEnabled = false
            
            // Safely fetches the key from GitHub Secrets environment during build
            val apiKey = System.getenv("AIzaSyCFU9BvtAYQkxP5fM7mbhNWQEgCU-vuX4E") ?: "MOCK_KEY"
            buildConfigField("String", "AIzaSyCFU9BvtAYQkxP5fM7mbhNWQEgCU-vuX4E", "\"$apiKey\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Jetpack Compose Components (Upgraded BOM to resolve HorizontalDivider & automirrored)
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Lifecycle and Background Execution
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Networking & REST Communications
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi JSON parser engine dependencies
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    
    // Moshi Converter for Retrofit integration
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
}