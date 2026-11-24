package io.solidar.donation.data.remote

import io.solidar.donation.platformDefaultBackendUrl

object ApiConfig {
    /**
     * URL base configurável.
     * Prioridades:
     * 1. [customBaseUrl] definido em runtime ou em testes
     * 2. [platformDefaultBackendUrl] resolvido via `expect`/`actual` para cada plataforma
     */
    var customBaseUrl: String? = null

    val baseUrl: String
        get() = customBaseUrl ?: platformDefaultBackendUrl()
}
