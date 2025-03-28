package io.github.kodepix.kmicro.service

import io.github.kodepix.*
import java.nio.file.*


val config by extractConfig<Config>()


data class Config(
    val service: ServiceConfig,
) {
    data class ServiceConfig(val deployment: DeploymentConfig, val storage: PathConfig) {
        data class DeploymentConfig(val port: Int)
        data class PathConfig(val path: Path)
    }
}
