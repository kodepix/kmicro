package io.github.kodepix.kmicro.service.plugins

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.application.*
import io.ktor.server.engine.BaseApplicationResponse.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.cio.*


internal fun Application.configureRouting() {

    install(AutoHeadResponse)

    install(StatusPages) {
        exception<BodyLengthIsTooLong> { call, cause -> call.application.log.warn(cause.message) }
        exception<BodyLengthIsTooSmall> { call, cause -> call.application.log.warn(cause.message) }
        exception<ChannelWriteException> { call, cause -> call.application.log.warn(cause.message) }
        exception<BadRequestException> { call, cause -> call.respondError(BadRequest, cause) }
        exception<Throwable> { call, cause -> call.respondError(InternalServerError, cause) }
    }
}


private suspend fun ApplicationCall.respondError(statusCode: HttpStatusCode, cause: Throwable) {
    respondText(text = "${statusCode.value}: ${cause.message}", status = statusCode)
    application.log.error(cause.message, cause)
}
