package io.github.kodepix.kmicro.service.plugins

import com.sksamuel.cohort.*
import com.sksamuel.cohort.logback.*
import com.sksamuel.cohort.system.*
import io.github.kodepix.kmicro.service.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.SwitchingProtocols
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.system.*
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics
import io.micrometer.prometheusmetrics.*
import io.micrometer.prometheusmetrics.PrometheusConfig.*
import kotlinx.coroutines.Dispatchers.IO
import java.nio.file.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


fun Application.configureMonitoring() {

    install(CallLogging) {
        disableDefaultColors()
        filter {
            val status = it.response.status()
            it.request.path().startsWith("/") && (status == null || !status.isSuccess()) && status != SwitchingProtocols
        }
    }

    val micrometerRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Cohort) {
        jvmInfo = true
        logManager = LogbackManager
        healthcheck(
            "/health",
            HealthCheckRegistry(IO) {
                register(DiskSpaceHealthCheck(Files.getFileStore(config.service.storage.path), minFreeSpacePercentage = 1.0), 10.seconds, 1.hours)
            }
        )
    }

    install(MicrometerMetrics) {
        registry = micrometerRegistry
        meterBinders = listOf(
            ProcessorMetrics(),
            DiskSpaceMetrics(config.service.storage.path.toFile()),
            JvmGcMetrics(),
            JvmHeapPressureMetrics(),
            JvmMemoryMetrics(),
            JvmThreadMetrics(),
        )
    }

    routing {
        get("/metrics") { call.respond(micrometerRegistry.scrape()) }
    }
}
