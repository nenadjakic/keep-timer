package com.github.nenadjakic.keeptimer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "keep-timer",
    ) {
        App()
    }
}