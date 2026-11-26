package io.solidar.donation.data.storage

import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.model.UserSession
import io.solidar.donation.readPlatformFileText
import io.solidar.donation.writePlatformFileText
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface TokenStorage {
    fun getToken(): String?

    fun saveSession(session: UserSession)

    fun getSession(): UserSession?

    fun clear()
}

@Serializable
internal data class UserSessionCacheDto(
    val token: String,
    val tipo: String,
    val id: Int,
    val nome: String,
    val email: String,
)

class InMemoryTokenStorage : TokenStorage {
    private var currentSession: UserSession? = null

    override fun getToken(): String? = currentSession?.token

    override fun saveSession(session: UserSession) {
        currentSession = session
    }

    override fun getSession(): UserSession? = currentSession

    override fun clear() {
        currentSession = null
    }
}

/**
 * Armazena a sessão autenticada no diretório nativo de arquivos da plataforma (`filesDir` no Android,
 * `~/.solidario_cache` no Desktop/JVM, `NSUserDefaults` no iOS) para que o app consiga reabrir
 * sem rede diretamente no conteúdo salvo em cache.
 */
class PersistentTokenStorage(
    private val json: Json = HttpClientFactory.defaultJson,
    private val reader: (String) -> String? = ::readPlatformFileText,
    private val writer: (String, String) -> Unit = ::writePlatformFileText,
) : TokenStorage {
    private var currentSession: UserSession? = carregarDoDisco()

    override fun getToken(): String? = currentSession?.token

    override fun saveSession(session: UserSession) {
        currentSession = session
        val dto =
            UserSessionCacheDto(
                token = session.token,
                tipo = session.tipo,
                id = session.id,
                nome = session.nome,
                email = session.email,
            )
        runCatching {
            writer(ARQUIVO_SESSAO, json.encodeToString(dto))
        }
    }

    override fun getSession(): UserSession? = currentSession

    override fun clear() {
        currentSession = null
        runCatching {
            writer(ARQUIVO_SESSAO, "")
        }
    }

    private fun carregarDoDisco(): UserSession? {
        val raw = reader(ARQUIVO_SESSAO)?.trim()
        if (raw.isNullOrEmpty()) return null
        return runCatching {
            val dto = json.decodeFromString<UserSessionCacheDto>(raw)
            if (dto.token.isBlank() && dto.id == 0) {
                null
            } else {
                UserSession(
                    token = dto.token,
                    tipo = dto.tipo,
                    id = dto.id,
                    nome = dto.nome,
                    email = dto.email,
                )
            }
        }.getOrNull()
    }

    companion object {
        const val ARQUIVO_SESSAO = "sessao_usuario_cache.json"
    }
}

object SessionStorage {
    val instance: TokenStorage by lazy { PersistentTokenStorage() }
}
