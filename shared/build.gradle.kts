plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    androidTarget()
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material)
                implementation(libs.compose.ui)
                implementation(libs.compose.resources)
                implementation(libs.compose.icons)
                
                // Core settings dependency is all that's needed
                implementation(libs.multiplatformSettings)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activityCompose)
            }
        }
        val androidUnitTest by getting

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // The -compose-helper dependency does not exist and should be removed
            }
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
}
