// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android.gradle.plugin) apply false // Added Hilt plugin
    alias(libs.plugins.ksp) apply false // Added KSP for Room
    alias(libs.plugins.kotlin.kapt) apply false // Added KAPT for Hilt

    kotlin("plugin.serialization") version "2.2.10" apply false

}