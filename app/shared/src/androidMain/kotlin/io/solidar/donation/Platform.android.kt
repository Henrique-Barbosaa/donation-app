package io.solidar.donation

import android.content.Context
import android.os.Build
import io.solidar.donation.data.remote.GeneratedBackendConfig
import java.io.File

object AndroidPlatformContext {
    @Volatile
    var filesDirPath: String? = null

    fun init(context: Context) {
        filesDirPath = context.applicationContext.filesDir.absolutePath
    }
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private fun isAndroidEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val product = Build.PRODUCT.lowercase()
    val hardware = Build.HARDWARE.lowercase()
    return fingerprint.startsWith("generic") ||
        fingerprint.contains("emulator") ||
        model.contains("google_sdk") ||
        model.contains("emulator") ||
        model.contains("sdk_gphone") ||
        product.contains("sdk") ||
        product.contains("emulator") ||
        hardware.contains("goldfish") ||
        hardware.contains("ranchu")
}

actual fun platformDefaultBackendUrl(): String {
    val host =
        if (!GeneratedBackendConfig.IS_CUSTOM_HOST && isAndroidEmulator()) {
            "10.0.2.2:8080"
        } else {
            GeneratedBackendConfig.ANDROID_DEFAULT_HOST
        }
    return "http://$host"
}

actual fun platformStorageDir(): String {
    val dirPath =
        AndroidPlatformContext.filesDirPath
            ?: File(System.getProperty("java.io.tmpdir") ?: ".", "solidario_android_cache").absolutePath
    val dir = File(dirPath)
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
