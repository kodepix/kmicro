package io.github.kodepix.kmicro.service.plugins

import io.github.kodepix.kmicro.*
import io.github.kodepix.kmicro.service.*
import io.github.smiley4.ktoropenapi.*
import io.github.smiley4.ktoropenapi.config.*
import io.github.smiley4.ktoropenapi.config.ServerConfig
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*


internal fun Application.configureOpenApi(config: OpenAPIConfig, buildVersion: String) {

    install(OpenApi) {

        info {
            version = buildVersion
            apply(config)
        }

        server { apply(config) }
        security { apply(config) }

        tags {
            val tagInfos = (config.tags + systemApiTags).onEach {
                tag(it.title) { description = it.description }
            }

            tagGenerator = { url ->
                tagInfos
                    .filter { url.firstOrNull() in it.paths }
                    .map { it.title }
                    .ifEmpty { listOf(config.tags[0].title) }
            }
        }

        pathFilter = { _, path -> path.firstOrNull() != DOCS && path.firstOrNull() != STATIC }
    }

    routing {
        rapiDoc(path = "/$DOCS", logoCaption = config.rapiDocServiceTitle)
        route("api.json") { openApi() }
    }
}

private fun InfoConfig.apply(config: OpenAPIConfig) = config applyTo this
private fun SecurityConfig.apply(config: OpenAPIConfig) = config applyTo this
private fun ServerConfig.apply(config: OpenAPIConfig) = config applyTo this


private const val DOCS = "docs"
internal const val STATIC = "static"

private val systemApiTags = listOf(
    TagInfo(
        title = KmicroMessages.monitoringTagTitle.toString(),
        description = KmicroMessages.monitoringTagDescription.toString(),
        paths = listOf("health", "metrics")
    ),
    TagInfo(
        title = KmicroMessages.debugTagTitle.toString(),
        description = KmicroMessages.debugTagDescription.toString(),
        paths = listOf("debug", "cohort")
    ),
)


/**
 * Provides a ready-made route for viewing documents using RapiDoc at the specified [path].
 *
 * @param path document resource path
 * @param logoCaption logo header
 * @param pageTitle title of the webpage you want to display in your documents
 * @param specUrl url to point RapiDoc to an OpenAPI JSON
 */
internal fun Route.rapiDoc(
    path: String,
    logoCaption: String,
    pageTitle: String = "$logoCaption | ${KmicroMessages.rapiDocPageTitlePart}",
    specUrl: String = "api.json",
) {
    route(path) {
        get {
            call.respondHtml {

                head {
                    title { +pageTitle }
                    link {
                        rel = "icon"
                        href = "static/lib/favicon.svg"
                        type = "image/svg+xml"
                    }
                    meta { charset = "utf-8" }
                    script {
                        type = "module"
                        src = "static/lib/rapidoc-min.js"
                    }
                    style {
                        unsafe {
                            +"""
                            rapi-doc::part(section-navbar-tag) {
                                color: #99ddff;
                            }
                            """
                        }
                    }
                }

                body {
                    unsafe {
                        +"""
                    <rapi-doc 
                        spec-url="$specUrl"
                        render-style = "read"
                        show-method-in-nav-bar="as-colored-text"
                        persist-auth="true"
                        show-header="false"
                        allow-server-selection="false"
                        show-curl-before-try="true"
                        >
                        <div slot="nav-logo" style="display: flex; align-items: center; justify-content: left; margin-bottom: 5px"> 
                            <img src="static/lib/favicon.svg" style="width:50px; margin-right: 20px"><span style="color: white;font-size: x-large;"><b>$logoCaption</b></span>
                        </div>
                    </rapi-doc>
                    """
                    }
                }
            }
        }
    }
}


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
