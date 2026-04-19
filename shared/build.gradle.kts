plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

kotlin {
    androidTarget() // Correct way to declare the Android target in KMP
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting
        val androidUnitTest by getting

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.elsoft.kmp_symbolic_logic_game.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
