package io.solidar.donation.data.remote

import io.solidar.donation.getPlatform

object ApiConfig {
    /**
     * URL base configurável.
     * Prioridades:
     * 1. customBaseUrl definido em runtime no app
     * 2. Android: Host resolvido automaticamente pelo Gradle (via local.properties ou detecção de IP local da máquina)
     * 3. Desktop / iOS: "http://localhost:8080"
     */
    var customBaseUrl: String? = null

    val baseUrl: String
        get() {
            customBaseUrl?.let { return it }
            return if (getPlatform().name.startsWith("Android")) {
                "http://${GeneratedBackendConfig.ANDROID_DEFAULT_HOST}"
            } else {
                "http://localhost:8080"
            }
        }
}
