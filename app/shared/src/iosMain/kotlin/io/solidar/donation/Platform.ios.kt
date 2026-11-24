package io.solidar.donation

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun platformDefaultBackendUrl(): String = "http://localhost:8080"

actual fun platformStorageDir(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    return (paths.firstOrNull() as? String) ?: "ios_documents"
}

actual fun readPlatformFileText(fileName: String): String? = NSUserDefaults.standardUserDefaults.stringForKey("solidar_cache_$fileName")

actual fun writePlatformFileText(
    fileName: String,
    content: String,
) {
    NSUserDefaults.standardUserDefaults.setObject(content, forKey = "solidar_cache_$fileName")
}
