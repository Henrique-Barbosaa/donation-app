package io.solidar.donation

import io.solidar.donation.data.remote.GeneratedBackendConfig
import java.io.File

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun platformDefaultBackendUrl(): String =
    System.getenv("SOLIDAR_API_URL")?.trim()?.removeSuffix("/")
        ?: if (GeneratedBackendConfig.IS_CUSTOM_HOST) {
            "http://${GeneratedBackendConfig.ANDROID_DEFAULT_HOST}"
        } else {
            "http://localhost:8080"
        }

actual fun platformStorageDir(): String {
    val base = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: "."
    val dir = File(base, ".solidario_cache")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dir.absolutePath
}

actual fun readPlatformFileText(fileName: String): String? =
    runCatching {
        val file = File(platformStorageDir(), fileName)
        if (file.exists()) file.readText() else null
    }.getOrNull()

actual fun writePlatformFileText(
    fileName: String,
    content: String,
) {
    runCatching {
        val file = File(platformStorageDir(), fileName)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}
