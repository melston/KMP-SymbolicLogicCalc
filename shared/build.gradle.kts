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
                
                implementation(libs.multiplatformSettings)
                
                // Add Markdown Renderer from Maven Central
                implementation(libs.multiplatform.markdown.renderer)
                // For Material 2 themed apps
                implementation(libs.multiplatform.markdown.renderer.m2)
                // OR for Material 3 themed apps
                //implementation("com.mikepenz:multiplatform-markdown-renderer-m3:${version}")
                implementation(libs.multiplatform.markdown.renderer.code)
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
