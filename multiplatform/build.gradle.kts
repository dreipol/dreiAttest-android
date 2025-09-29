import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    id("maven-publish")
    alias(libs.plugins.kotlin.serialization)
}

group = "ch.dreipol.dreiattest.multiplatform"


kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvmToolchain(libs.versions.jvm.version.get().toInt())

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.version.get()))
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework()
    }


    android() {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.kermit)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.multiplatform.settings.no.arg)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.ktor.client.mock)
            implementation(libs.multiplatform.settings.test)
        }

        androidMain.dependencies {
            implementation(libs.integrity)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.ktor.client.android)
        }

        androidUnitTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "ch.dreipol.dreiattest.multiplatform"

    compileSdk = 35
    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    buildTypes {
        //This is for MultiplatformSettings
        getByName("debug") {
            matchingFallbacks.add("release")
            // MPP libraries don't currently get this resolution automatically
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}


// workaround for https://youtrack.jetbrains.com/issue/KT-27170
//configurations {
//    compileClasspath
//}

//        TODO: READD

fun getGPRCredentials(): Pair<String, String>? {
    val properties = Properties()
    val localProperties = File(rootProject.projectDir.absolutePath + "/local.properties")
    var user: String? = null
    var password: String? = null
    if (localProperties.exists()) {
        properties.load(localProperties.reader())

        user = properties.getProperty("gpr.user")
        password = properties.getProperty("gpr.key")
    }
    return if (user != null && password != null) {
        Pair(user, password)
    } else {
        null
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dreipol/dreiAttest-android")
            val credentials = getGPRCredentials()
            credentials {
                username = credentials?.first ?: System.getenv("GITHUB_USERNAME") ?: ""
                password = credentials?.second ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
