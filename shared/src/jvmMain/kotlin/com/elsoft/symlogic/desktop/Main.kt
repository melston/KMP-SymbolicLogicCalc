package com.elsoft.symlogic.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.elsoft.symlogic.ui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Symbolic Logic Game") {
        App()
    }
}
