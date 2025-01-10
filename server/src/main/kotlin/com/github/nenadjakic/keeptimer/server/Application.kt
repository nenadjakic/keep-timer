package com.github.nenadjakic.keeptimer.server

import com.github.nenadjakic.keeptimer.Greeting
import com.github.nenadjakic.keeptimer.SERVER_PORT
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

}