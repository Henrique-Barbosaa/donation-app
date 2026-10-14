import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    android {
        namespace = "io.solidar.donation.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/backendConfig/kotlin"))
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.json)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

abstract class GenerateBackendConfigTask : DefaultTask() {
    @get:Internal
    abstract val localProperties: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        var customUrl: String? = null
        val propFile = localProperties.orNull?.asFile
        if (propFile != null && propFile.exists()) {
            val props = Properties()
            propFile.inputStream().use { props.load(it) }
            customUrl = props.getProperty("backend.url") ?: props.getProperty("backend.host")
        }

        val host =
            if (!customUrl.isNullOrBlank()) {
                customUrl.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
            } else {
                resolveLocalIp()
            }

        val packageDir = outputDir.get().asFile.resolve("io/solidar/donation/data/remote")
        packageDir.mkdirs()
        packageDir.resolve("GeneratedBackendConfig.kt").writeText(
            """
            |package io.solidar.donation.data.remote
            |
            |// Gerado automaticamente pelo Gradle com base no IP local da maquina ou local.properties
            |internal object GeneratedBackendConfig {
            |    const val ANDROID_DEFAULT_HOST: String = "$host"
            |}
            |
            """.trimMargin(),
        )
    }

    private fun resolveLocalIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            val ips = mutableListOf<String>()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress
                        if (!host.startsWith("172.") && !host.startsWith("169.254.")) {
                            ips.add(host)
                        }
                    }
                }
            }
            val preferredIp = ips.firstOrNull { it.startsWith("192.168.") || it.startsWith("10.") } ?: ips.firstOrNull()
            if (preferredIp != null) {
                return "$preferredIp:8080"
            }
        } catch (_: Exception) {
        }
        return "10.0.2.2:8080"
    }
}

val generateBackendConfig =
    tasks.register<GenerateBackendConfigTask>("generateBackendConfig") {
        description = "Gera a classe com o host do backend resolvido (via local.properties ou deteccao de IP)"
        val localProps = rootProject.file("local.properties")
        if (localProps.exists()) {
            localProperties.set(localProps)
        }
        outputDir.set(layout.buildDirectory.dir("generated/backendConfig/kotlin"))
    }

tasks.matching {
    it.name.startsWith("compile") || it.name.startsWith("generateComposeResClass")
}.configureEach {
    dependsOn(generateBackendConfig)
}
