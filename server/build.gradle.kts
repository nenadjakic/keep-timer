plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.github.nenadjakic.keeptimer"
version = "1.0.0"
application {
    mainClass.set("com.github.nenadjakic.keeptimer.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.serialization.gson)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}