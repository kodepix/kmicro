import com.github.benmanes.gradle.versions.updates.*
import com.vanniktech.maven.publish.SonatypeHost.Companion.CENTRAL_PORTAL

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka)
    alias(libs.plugins.i18n4k)
    alias(libs.plugins.vanniktech.maven.publish)
    signing
}

description = "Microservice framework that uses Ktor."
group = "io.github.kodepix"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.slf4j)
    api(libs.kotlin.logging)
    api(libs.ktor.openapi)
    api(libs.ktor.server.auto.head.response)
    api(libs.ktor.server.call.logging)
    api(libs.ktor.server.cio)
    api(libs.ktor.server.compression)
    api(libs.ktor.server.cors)
    api(libs.ktor.server.default.headers)
    api(libs.ktor.server.html.builder)
    api(libs.ktor.server.metrics.micrometer)
    api(libs.ktor.server.status.pages)
    api(libs.kodepix.ktor.server.extras)
    api(libs.cohort.ktor)
    api(libs.cohort.logback)
    api(libs.cohort.micrometer)
    api(libs.logback.classic)
    api(libs.micrometer.registry.prometheus)

    testImplementation(libs.bundles.testing)
}

kotlin { jvmToolchain(21) }

ktlint {
    verbose = true
    outputToConsole = true
    filter { exclude { it.file.path.contains("generated") } }
}

i18n4k { sourceCodeLocales = listOf("en", "ru") }

mavenPublishing {
    publishToMavenCentral(CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        name = "Microservice framework that uses Ktor"
        description = project.description
        inceptionYear = "2025"
        url = "https://github.com/kodepix/${project.name}/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kodepix"
                name = "kodepix"
                url = "https://github.com/kodepix/"
            }
        }
        scm {
            url = "https://github.com/kodepix/${project.name}/"
            connection = "scm:git:git://github.com/kodepix/${project.name}.git"
            developerConnection = "scm:git:git://github.com/kodepix/${project.name}.git"
        }
    }
}

signing {
    // Used while error "invalid header encountered" is not fixed (https://github.com/vanniktech/gradle-maven-publish-plugin/issues/900)
    val signingPassword: String? by project
    val signingSecretKeyRingFile: String? by project
    useInMemoryPgpKeys(files(signingSecretKeyRingFile).single().readText(), signingPassword)
}

tasks {
    test { useJUnitPlatform() }

    withType<DependencyUpdatesTask> {
        rejectVersionIf { isNonStable(candidate.version) }
    }

    runKtlintCheckOverKotlinScripts { dependsOn(runKtlintFormatOverKotlinScripts) }
    runKtlintCheckOverMainSourceSet { dependsOn(runKtlintFormatOverMainSourceSet) }
    runKtlintCheckOverTestSourceSet { dependsOn(runKtlintFormatOverTestSourceSet) }
    runKtlintFormatOverMainSourceSet { dependsOn(generateI18n4kFiles) }
    dokkaGeneratePublicationHtml { dependsOn(generateI18n4kFiles) }
}

versionCatalogUpdate {
    pin {
        plugins.addAll(libs.plugins.version.catalog.update) // because nl.littlerobots.version-catalog-update:1.0.0 not updates libs versions
    }
}

private fun isNonStable(version: String) = run {
    val versionIsStable = stableKeywords.any { version.uppercase().contains(it) }
    val isStable = versionIsStable || versionRegex.matches(version)
    !isStable
}

private val stableKeywords = listOf("RELEASE", "FINAL", "GA")
private val versionRegex = Regex("^[0-9,.v-]+(-r)?$")
