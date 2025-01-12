package com.github.nenadjakic.keeptimer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "keep-timer",
        state = WindowState(width = 500.dp, height = 500.dp)
    ) {
        App()
    }
}