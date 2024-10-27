plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.facefingerprintregister"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.facefingerprintregister"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // For CameraX
    implementation ("androidx.camera:camera-core:1.0.2")
    implementation ("androidx.camera:camera-camera2:1.0.2")
    implementation ("androidx.camera:camera-lifecycle:1.0.2")
    implementation ("androidx.camera:camera-view:1.0.0-alpha30")

// For Biometric Authentication
    implementation ("androidx.biometric:biometric:1.1.0")

// SQLite Database (Already included in Android SDK, no extra dependency needed)

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0") // Use the updated version
    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.1.0")
    // MediaPlayer dependencies
    implementation("androidx.media:media:1.3.1")

    // Google ML Kit for face detection
    implementation("com.google.mlkit:face-detection:16.0.3")

    // Add this for ListenableFuture support in CameraX
    implementation("androidx.concurrent:concurrent-futures:1.1.0")

    // Material Components (ensure you have a version that supports Material 3)
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.guava:guava:31.1-android")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}

