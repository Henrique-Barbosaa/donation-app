package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    val defaultJson: Json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
            encodeDefaults = true
        }

    fun create(
        engine: HttpClientEngine? = null,
        tokenProvider: () -> String? = { null },
    ): HttpClient {
        val configure: HttpClientConfig<*>.() -> Unit = {
            expectSuccess = true

            install(ContentNegotiation) {
                json(defaultJson)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }

            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("KTOR_HTTP: $message")
                        }
                    }
                level = LogLevel.INFO
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                tokenProvider()?.let { token ->
                    if (token.isNotBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
            }
        }

        return if (engine != null) {
            HttpClient(engine, configure)
        } else {
            HttpClient(configure)
        }
    }
}
