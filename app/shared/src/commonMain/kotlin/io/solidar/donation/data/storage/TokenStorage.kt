package io.solidar.donation.data.storage

import io.solidar.donation.model.UserSession

interface TokenStorage {
    fun getToken(): String?

    fun saveSession(session: UserSession)

    fun getSession(): UserSession?

    fun clear()
}

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
