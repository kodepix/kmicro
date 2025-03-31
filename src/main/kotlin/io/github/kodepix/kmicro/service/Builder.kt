package io.github.kodepix.kmicro.service

import io.github.smiley4.ktoropenapi.config.*
import io.github.smiley4.ktoropenapi.config.ServerConfig
import io.ktor.server.application.*


/**
 * Service configuration builder.
 */
@ServiceBuilderDsl
class ServiceBuilder internal constructor() {

    /**
     * Service configuration.
     *
     * @param block configuration block
     */
    fun service(block: ServiceConfig.() -> Unit) {
        serviceConfig = block
    }

    internal var serviceConfig: ServiceConfig.() -> Unit = {}
        private set


    /**
     * OpenAPI service configuration.
     *
     * @param block configuration block
     */
    fun openAPI(block: OpenAPIConfig.() -> Unit) = apis.append(block)
    internal infix fun applyTo(config: OpenAPIConfig) = apis.applyTo(config)
    private val apis = ConfigsHolder<OpenAPIConfig>()

    /**
     * Ktor application configuration.
     *
     * @param block configuration block
     */
    fun module(block: Application.() -> Unit) = modules.append(block)
    internal infix fun applyTo(config: Application) = modules.applyTo(config)
    private val modules = ConfigsHolder<Application>()
}


/**
 * Service configuration.
 */
@ServiceBuilderDsl
class ServiceConfig internal constructor() {
    /**
     * Service title.
     */
    var title = "API"

    /**
     * Build version.
     */
    var buildVersion = ""

    /**
     * ASCII logo printed in log.
     */
    var bannerLogo = ""

    /**
     * Service copyright holder.
     */
    var copyrightHolder = ""

    /**
     * Messages locale.
     */
    var locale: String? = null
}


/**
 * OpenAPI service configuration.
 */
@OpenAPIConfigDsl
class OpenAPIConfig internal constructor() {

    /**
     * Service OpenAPI main title.
     */
    var rapiDocServiceTitle = "Service"

    /**
     * Tag description list for the API.
     */
    var tags = emptyList<TagInfo>()

    /**
     * Basic information for the exposed API.
     *
     * @param block configuration block
     */
    fun info(block: InfoConfig.() -> Unit) = infos.append(block)
    internal infix fun applyTo(config: InfoConfig) = infos.applyTo(config)
    private val infos = ConfigsHolder<InfoConfig>()

    /**
     * An object representing a Server.
     *
     * @param block configuration block
     */
    fun server(block: ServerConfig.() -> Unit) = servers.append(block)
    internal infix fun applyTo(config: ServerConfig) = servers.applyTo(config)
    private val servers = ConfigsHolder<ServerConfig>()

    /**
     * Configuration for security and authentication.
     *
     * @param block configuration block
     */
    fun security(block: SecurityConfig.() -> Unit) = securities.append(block)
    internal infix fun applyTo(config: SecurityConfig) = securities.applyTo(config)
    private val securities = ConfigsHolder<SecurityConfig>()
}


/**
 * Stores service configuration functions.
 */
private class ConfigsHolder<T> {

    fun append(block: T.() -> Unit) {
        configs += block
    }

    fun applyTo(config: T) = configs.forEach { it(config) }

    private val configs = mutableListOf<T.() -> Unit>()
}


@DslMarker
annotation class ServiceBuilderDsl

@DslMarker
annotation class OpenAPIConfigDsl


/**
 * Tag description for the API.
 *
 * @property title text
 * @property description description
 * @property paths list of url strings that are tagged for the route
 */
data class TagInfo(
    val title: String,
    val description: String,
    val paths: List<String> = emptyList(),
)
