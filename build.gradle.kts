plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false) // for kotlin {} block
    alias(libs.plugins.android.kmp.library).apply(false) // for android {} block
    alias(libs.plugins.maven.publish).apply(false) // for publishing {} block
    alias(libs.plugins.compose.multiplatform).apply(false) // for compose {} block
    alias(libs.plugins.compose.compiler).apply(false) // for composeCompiler {} block
}
