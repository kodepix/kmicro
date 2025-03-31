package io.github.kodepix.kmicro.service.plugins

import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Options
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*


internal fun Application.configureHTTP() {

    install(DefaultHeaders) {
        header("X-Engine", "Kmicro")
    }

    install(CORS) {
        allowMethod(Options)
        allowMethod(Put)
        allowMethod(Delete)
        allowMethod(Patch)
        allowHeader(Authorization)
        allowHeader(ContentType)
        allowOrigins { true }
    }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
}
