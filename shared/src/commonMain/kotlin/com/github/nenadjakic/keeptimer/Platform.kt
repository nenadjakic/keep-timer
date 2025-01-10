package com.github.nenadjakic.keeptimer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform