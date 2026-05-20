package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.elsoft.symlogic.ui.util.AppContext

@Composable
fun App() {
    // Initialize the global context for Android-specific needs like resource loading
    AppContext = LocalContext.current.applicationContext
    
    // Call the common App composable
    App()
}
