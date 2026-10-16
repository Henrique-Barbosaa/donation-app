package io.solidar.donation.util

/**
 * Regras de validação dos formulários como funções puras.
 *
 * A tela deriva a validade a partir do estado atual (sem `remember` extra) e
 * as mesmas funções podem ser testadas isoladamente na Sprint 2.
 */
object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val SENHA_MIN = 6
    const val NOME_ITEM_MIN = 3
    const val NOME_ITEM_MAX = 80
    const val DESCRICAO_MAX = 500

    fun emailValido(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun erroEmail(email: String): String? =
        when {
            email.isBlank() -> null
            !emailValido(email) -> "Informe um e-mail válido (ex: nome@dominio.com)."
            else -> null
        }

    fun erroSenha(senha: String): String? =
        when {
            senha.isEmpty() -> null
            senha.length < SENHA_MIN -> "A senha deve ter pelo menos $SENHA_MIN caracteres."
            else -> null
        }

    fun loginValido(
        email: String,
        senha: String,
    ): Boolean = emailValido(email) && senha.length >= SENHA_MIN

    fun cpfValido(cpf: String): Boolean = cpf.length == 11 && cpf.all { it.isDigit() }

    fun cnpjValido(cnpj: String): Boolean = cnpj.length == 14 && cnpj.all { it.isDigit() }

    fun signUpValido(
        isPf: Boolean,
        nome: String,
        email: String,
        document: String,
        senha: String,
    ): Boolean {
        val nomeValido = nome.trim().length >= 3
        val emailVal = emailValido(email)
        val docValido = if (isPf) cpfValido(document) else cnpjValido(document)
        val senhaValida = senha.length >= SENHA_MIN
        return nomeValido && emailVal && docValido && senhaValida
    }
}

/** Estado do formulário de nova doação. */
data class DoacaoForm(
    val nome: String = "",
    val quantidade: Int = 1,
    val categoria: String = "Roupas",
    val eNovo: Boolean = false,
    val descricao: String = "",
    val motivo: String = "",
)

/** Erros derivados do [DoacaoForm]; `null` significa campo válido. */
data class DoacaoFormErros(
    val nome: String? = null,
    val quantidade: String? = null,
    val categoria: String? = null,
    val descricao: String? = null,
) {
    val valido: Boolean
        get() = nome == null && quantidade == null && categoria == null && descricao == null
}

fun validarDoacao(form: DoacaoForm): DoacaoFormErros {
    val nome = form.nome.trim()
    return DoacaoFormErros(
        nome =
            when {
                nome.isEmpty() -> "Informe o que você está doando."
                nome.length < Validators.NOME_ITEM_MIN -> "Use pelo menos ${Validators.NOME_ITEM_MIN} caracteres."
                nome.length > Validators.NOME_ITEM_MAX -> "Use no máximo ${Validators.NOME_ITEM_MAX} caracteres."
                else -> null
            },
        quantidade = if (form.quantidade < 1) "A quantidade deve ser pelo menos 1." else null,
        categoria = if (form.categoria.isBlank()) "Escolha uma categoria." else null,
        descricao =
            if (form.descricao.length > Validators.DESCRICAO_MAX) {
                "A descrição pode ter no máximo ${Validators.DESCRICAO_MAX} caracteres."
            } else {
                null
            },
    )
}
