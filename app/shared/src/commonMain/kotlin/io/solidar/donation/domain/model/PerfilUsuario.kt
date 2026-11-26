package io.solidar.donation.domain.model

/**
 * Entidade pura de domínio que representa o perfil completo do usuário autenticado
 * (Pessoa Física ou Instituição).
 */
data class PerfilUsuario(
    val id: Int,
    val nome: String,
    val email: String,
    val tipo: String,
    val documento: String = "",
    val dataNascimento: String? = null,
    val site: String? = null,
    val logradouro: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cep: String = "",
) {
    val isInstituicao: Boolean
        get() = tipo.equals("INSTITUICAO", ignoreCase = true)

    val tipoExibicao: String
        get() = if (isInstituicao) "Instituição / ONG" else "Pessoa Física"

    val iniciais: String
        get() {
            val partes = nome.trim().split(" ").filter { it.isNotBlank() }
            return when {
                partes.isEmpty() -> "US"
                partes.size == 1 -> partes.first().take(2).uppercase()
                else -> "${partes.first().first()}${partes.last().first()}".uppercase()
            }
        }

    val possuiEndereco: Boolean
        get() =
            logradouro.isNotBlank() ||
                numero.isNotBlank() ||
                bairro.isNotBlank() ||
                cep.isNotBlank()

    val enderecoFormatado: String
        get() {
            if (!possuiEndereco) return "Endereço ainda não informado"
            val linhaRua =
                listOfNotNull(
                    logradouro.takeIf { it.isNotBlank() },
                    numero.takeIf { it.isNotBlank() }?.let { "nº $it" },
                ).joinToString(", ")
            val cepLimpo = cep.filter { it.isDigit() }
            val cepFmt =
                if (cepLimpo.length == 8) {
                    "${cepLimpo.substring(0, 5)}-${cepLimpo.substring(5, 8)}"
                } else {
                    cep
                }
            val linhaBairroCep =
                listOfNotNull(
                    bairro.takeIf { it.isNotBlank() },
                    cepFmt.takeIf { it.isNotBlank() }?.let { "CEP $it" },
                ).joinToString(" • ")
            return listOfNotNull(
                linhaRua.takeIf { it.isNotBlank() },
                linhaBairroCep.takeIf { it.isNotBlank() },
            ).joinToString("\n")
        }

    val documentoFormatado: String
        get() {
            val digitos = documento.filter { it.isDigit() }
            return when (digitos.length) {
                11 ->
                    "${digitos.substring(0, 3)}.${digitos.substring(3, 6)}." +
                        "${digitos.substring(6, 9)}-${digitos.substring(9, 11)}"
                14 ->
                    "${digitos.substring(0, 2)}.${digitos.substring(2, 5)}." +
                        "${digitos.substring(5, 8)}/${digitos.substring(8, 12)}-${digitos.substring(12, 14)}"
                else -> documento
            }
        }
}

/** Dados de entrada do domínio para atualização cadastral (`PUT /api/usuarios/perfil`). */
data class AtualizarPerfilInput(
    val nome: String,
    val logradouro: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cep: String = "",
)

/** Dados de entrada do domínio para alteração de senha (`PUT /api/usuarios/senha`). */
data class AlterarSenhaInput(
    val senhaAtual: String,
    val novaSenha: String,
)
