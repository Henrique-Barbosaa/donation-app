package io.solidar.donation.ui.auth

fun isEmailValid(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
    return email.isNotBlank() && emailRegex.matches(email)
}

fun isPasswordValid(password: String): Boolean {
    return password.isNotBlank() && password.length >= 6
}

fun isCpfValid(cpf: String): Boolean {
    val cleanCpf = cpf.filter { it.isDigit() }
    return cleanCpf.length == 11
}

fun isCnpjValid(cnpj: String): Boolean {
    val cleanCnpj = cnpj.filter { it.isDigit() }
    return cleanCnpj.length == 14
}
