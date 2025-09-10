plugins {
    alias(libs.plugins.android.library)
    id("kotlin-android")
    id("kotlin-kapt")
}

group = "ch.dreipol.attest.android"
version = "1.0"

android {
    compileSdk = 35
    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
//            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
    }

    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
    namespace = "ch.dreipol.attest.android"

    compileOptions {
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")

    testImplementation("junit:junit:4.13.2")
}
