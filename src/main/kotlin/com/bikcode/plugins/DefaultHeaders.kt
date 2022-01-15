package com.bikcode.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import java.time.Duration


fun Application.configureDefaultHeaders() {
    install(DefaultHeaders) {
        val oneYearInSeconds = Duration.ofDays(365).seconds
        header(
            name = HttpHeaders.CacheControl,
            value = "public, max-age=$oneYearInSeconds, immutable"
        )
    }
}