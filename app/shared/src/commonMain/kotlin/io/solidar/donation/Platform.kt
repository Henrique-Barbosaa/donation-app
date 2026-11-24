package io.solidar.donation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/** Endereço padrão do backend para cada plataforma (Android, Desktop/JVM, iOS). */
expect fun platformDefaultBackendUrl(): String

/** Diretório de arquivos locais do aplicativo na plataforma (`filesDir` no Android). */
expect fun platformStorageDir(): String

/** Lê o conteúdo textual de um arquivo de cache local na pasta da plataforma, ou `null` se não existir. */
expect fun readPlatformFileText(fileName: String): String?

/** Grava conteúdo textual em um arquivo na pasta de armazenamento local da plataforma. */
expect fun writePlatformFileText(
    fileName: String,
    content: String,
)
