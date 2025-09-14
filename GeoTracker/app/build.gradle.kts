plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // For Room
    alias(libs.plugins.kotlin.kapt) // For Hilt
    alias(libs.plugins.hilt.android.gradle.plugin)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}

android {
    namespace = "com.example.geotracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.geotracker"
        minSdk = 35
        targetSdk = 36
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.navigation.runtime.ktx)
    ksp(libs.androidx.room.compiler) // Room uses KSP
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.room:room-ktx:2.7.2")

    // Android Maps Compose composables for the Maps SDK for Android
    implementation("com.google.maps.android:maps-compose:6.7.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Hilt dependencies
    implementation(libs.hilt.android)
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    kapt(libs.hilt.compiler) // Hilt now uses KAPT
    implementation("androidx.navigation:navigation-compose:$2.9.4")

    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")


}