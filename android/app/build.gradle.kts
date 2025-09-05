plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // kotlin("kapt")
    // alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.homeal_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.homeal_app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        viewBinding = true
        compose = true  
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"  // ⭐ CHANGÉ À 1.5.3 (compatible avec Kotlin 1.9.10)
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ⭐ COMPOSE BOM COMPATIBLE AVEC KOTLIN 1.9.10
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))  // ⭐ VERSION COMPATIBLE
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.7.2")  // ⭐ VERSION COMPATIBLE
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.navigation:navigation-compose:2.7.2")  // ⭐ VERSION COMPATIBLE

    // Room database 
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // CameraX for camera preview
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Accompanist for permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // TESTING DEPENDENCIES
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.7")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.1")
}
