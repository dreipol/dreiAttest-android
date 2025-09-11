import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

group = "ch.dreipol.attest.android"
version = "1.0"

android {
    namespace = "ch.dreipol.attest.android"
    compileSdk = 35
    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro", getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xexplicit-api=strict")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

}

dependencies {
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
}
