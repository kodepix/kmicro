package io.github.kodepix.kmicro.service

import io.github.kodepix.*
import io.github.kodepix.kmicro.service.plugins.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*


/**
 * Configures and runs embedded server with [CIO] engine.
 *
 * Usage:
 *
 * ```kotlin
 * service {
 * }
 * ```
 *
 * @sample io.github.kodepix.kmicro.samples.serviceSample
 */
fun service(init: Application.(String) -> Unit) {

    val serviceName = extractServiceName()
    configureLogback(serviceName)

    log.info {
        buildString {
            +"Starting service"
            +DELIMITER
            deleteLastChar()
        }
    }

    embeddedServer(
        factory = CIO,
        configure = { connector { port = config.service.deployment.port } },
        module = {
            configureHTTP()
            configureMonitoring()
            configureRouting()
            init(serviceName)
        }
    )
        .start(wait = true)
}


private fun extractServiceName() = Class.forName(Throwable().stackTrace.last().className).protectionDomain.codeSource.location.path
    .let { Regex(".*/(.+)/build").find(it) ?: Regex("([^/-]+)-\\d.*jar").find(it) }
    .let { it?.groupValues?.get(1) ?: error("Can't extract service name") }

private fun configureLogback(serviceName: String) {
    System.setProperty("kmicro.service.name", serviceName)
    System.setProperty("kmicro.storage.path", config.service.storage.path.normalize().toAbsolutePath().toString())
}


internal const val DELIMITER = "—————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————"

private val log by logger()
