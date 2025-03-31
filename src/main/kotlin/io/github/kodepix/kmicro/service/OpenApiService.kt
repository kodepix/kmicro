package io.github.kodepix.kmicro.service

import io.github.kodepix.*
import io.github.kodepix.kmicro.*
import io.github.kodepix.kmicro.service.plugins.*
import io.github.kodepix.ktor.*
import io.github.kodepix.ktor.dsl.routing.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.cio.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.util.*


/**
 * Configures and runs embedded server with [CIO] engine and OpenAPI.
 *
 * Usage:
 *
 * ```kotlin
 * openApiService {
 *     openAPI {
 *     }
 * }
 * ```
 *
 * @sample io.github.kodepix.kmicro.samples.openApiServiceSample
 */
fun openApiService(init: ServiceBuilder.() -> Unit) = service { serviceName ->

    val serviceConfig = ServiceConfig()
    val combinedTitle by lazy { "${serviceConfig.title} :: ${serviceName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}" }

    val apiConfig = OpenAPIConfig()

    val builder = ServiceBuilder().apply {

        openAPI {
            info { title = combinedTitle }
            server { url = "/$serviceName" }
        }

        module {
            configureOpenApi(apiConfig, serviceConfig.buildVersion)
        }

        init()
        serviceConfig(serviceConfig)
    }

    if (serviceConfig.locale != null)
        ktorLocale(serviceConfig.locale!!)

    printBanner(combinedTitle, serviceConfig.bannerLogo, serviceConfig.copyrightHolder, serviceConfig.buildVersion)

    builder.applyTo(apiConfig)
    builder.applyTo(this)

    routing {
        staticResources("/$STATIC", "static")
        forAllOpenApiRoutes {
            response { BadRequest to { description = KmicroMessages.badRequestDescription.toString() } }
            response { InternalServerError to { description = KmicroMessages.internalServerErrorDescription.toString() } }
        }
    }
}


private fun printBanner(title: String, logo: String, copyrightHolder: String, buildVersion: String) {
    log.info {
        buildString {
            +""
            +DELIMITER
            if (logo.isNotEmpty())
                +logo.trimMargin()
            +"$title :: $buildVersion :: © $copyrightHolder"
            +DELIMITER
        }
    }
}

private val log by logger()
