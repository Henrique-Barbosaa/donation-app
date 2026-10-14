package io.solidar.donation.model

data class UserSession(
    val token: String,
    val tipo: String,
    val id: Int,
    val nome: String,
    val email: String,
)
