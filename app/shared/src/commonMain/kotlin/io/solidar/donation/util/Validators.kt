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
    const val NOME_ITEM_MAX = 45
    const val TITULO_CAMPANHA_MAX = 80
    const val DESCRICAO_ITEM_MAX = 200
    const val DESCRICAO_MAX = 500
    const val NOME_LOCAL_MAX = 45
    const val ITENS_FOCO_MAX = 200
    const val CEP_DIGITOS = 8

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
            if (form.descricao.length > Validators.DESCRICAO_ITEM_MAX) {
                "A descrição pode ter no máximo ${Validators.DESCRICAO_ITEM_MAX} caracteres."
            } else {
                null
            },
    )
}

/** Item individual no formulário de criação de campanha, com quantidade unitária própria. */
data class ItemCampanhaForm(
    val id: Int,
    val nome: String = "",
    val quantidade: Int = 1,
)

/** Local físico de coleta no formulário de criação de campanha. */
data class LocalColetaForm(
    val id: Int,
    val nome: String = "",
    val logradouro: String = "",
    val bairro: String = "",
    val numero: String = "",
    val cep: String = "",
) {
    val possuiAlgumCampo: Boolean
        get() =
            nome.isNotBlank() ||
                logradouro.isNotBlank() ||
                bairro.isNotBlank() ||
                numero.isNotBlank() ||
                cep.isNotBlank()
}

/** Estado do formulário de criação de campanha de doação. */
data class CampanhaForm(
    val titulo: String = "",
    val descricao: String = "",
    val pedirItens: Boolean = true,
    val pedirDinheiro: Boolean = false,
    val metaReaisTexto: String = "",
    val itens: List<ItemCampanhaForm> = listOf(ItemCampanhaForm(id = 1, nome = "", quantidade = 1)),
    val locais: List<LocalColetaForm> = emptyList(),
) {
    val itensPreenchidos: List<ItemCampanhaForm>
        get() = itens.filter { it.nome.isNotBlank() }

    val locaisPreenchidos: List<LocalColetaForm>
        get() = locais.filter { it.nome.isNotBlank() }

    val totalUnidades: Int
        get() = if (pedirItens) itensPreenchidos.sumOf { it.quantidade.coerceAtLeast(1) } else 0

    val metaEmCentavos: Long
        get() =
            if (pedirDinheiro) {
                (metaReaisTexto.filter { it.isDigit() }.toLongOrNull() ?: 0L) * 100L
            } else {
                0L
            }

    val itensFocoFormatado: String
        get() =
            if (!pedirItens) {
                ""
            } else {
                itensPreenchidos.joinToString(", ") { item ->
                    "${item.nome.trim()} (${item.quantidade.coerceAtLeast(1)} un.)"
                }
            }

    fun proximoItemId(): Int = (itens.maxOfOrNull { it.id } ?: 0) + 1

    fun proximoLocalId(): Int = (locais.maxOfOrNull { it.id } ?: 0) + 1
}

/** Erros derivados do [CampanhaForm]; `null` significa campo válido. */
data class CampanhaFormErros(
    val titulo: String? = null,
    val descricao: String? = null,
    val tipoArrecadacao: String? = null,
    val itens: String? = null,
    val metaDinheiro: String? = null,
    val locais: String? = null,
) {
    val valido: Boolean
        get() =
            titulo == null &&
                descricao == null &&
                tipoArrecadacao == null &&
                itens == null &&
                metaDinheiro == null &&
                locais == null
}

fun validarCampanha(form: CampanhaForm): CampanhaFormErros {
    val titulo = form.titulo.trim()
    val erroTipo =
        if (!form.pedirItens && !form.pedirDinheiro) {
            "Ative pelo menos um tipo de arrecadação (objetos ou dinheiro)."
        } else {
            null
        }
    val itensValidos = form.itensPreenchidos
    val erroItens =
        if (!form.pedirItens) {
            null
        } else {
            when {
                itensValidos.isEmpty() -> "Adicione pelo menos um item com nome."
                itensValidos.any { it.quantidade < 1 } -> "A quantidade de cada item deve ser pelo menos 1."
                form.itensFocoFormatado.length > Validators.ITENS_FOCO_MAX ->
                    "A lista de itens excede o limite de ${Validators.ITENS_FOCO_MAX} caracteres."
                else -> null
            }
        }
    val erroMetaDinheiro =
        if (!form.pedirDinheiro) {
            null
        } else if (form.metaEmCentavos < 100L) {
            "Informe uma meta financeira de pelo menos R$ 1,00."
        } else {
            null
        }
    val erroLocais =
        when {
            form.locais.any { it.possuiAlgumCampo && it.nome.isBlank() } ->
                "Informe o nome do local de coleta."
            form.locais.any { it.nome.trim().length > Validators.NOME_LOCAL_MAX } ->
                "O nome do local pode ter no máximo ${Validators.NOME_LOCAL_MAX} caracteres."
            form.locais.any { local ->
                val cepDigitos = local.cep.filter { it.isDigit() }
                cepDigitos.isNotEmpty() && cepDigitos.length != 8
            } -> "O CEP do local deve conter 8 dígitos."
            else -> null
        }
    return CampanhaFormErros(
        titulo =
            when {
                titulo.isEmpty() -> "Informe o título da campanha."
                titulo.length < Validators.NOME_ITEM_MIN -> "Use pelo menos ${Validators.NOME_ITEM_MIN} caracteres."
                titulo.length > Validators.TITULO_CAMPANHA_MAX -> "Use no máximo ${Validators.TITULO_CAMPANHA_MAX} caracteres."
                else -> null
            },
        descricao =
            if (form.descricao.length > Validators.DESCRICAO_MAX) {
                "A descrição pode ter no máximo ${Validators.DESCRICAO_MAX} caracteres."
            } else {
                null
            },
        tipoArrecadacao = erroTipo,
        itens = erroItens,
        metaDinheiro = erroMetaDinheiro,
        locais = erroLocais,
    )
}

/** Estado do formulário de edição de perfil do usuário. */
data class PerfilForm(
    val nome: String = "",
    val cep: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val bairro: String = "",
)

/** Erros derivados do [PerfilForm]; `null` significa campo válido. */
data class PerfilFormErros(
    val nome: String? = null,
    val cep: String? = null,
    val logradouro: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
) {
    val valido: Boolean
        get() =
            nome == null &&
                cep == null &&
                logradouro == null &&
                numero == null &&
                bairro == null
}

/** Validação pura do formulário de edição de perfil. */
fun validarPerfil(form: PerfilForm): PerfilFormErros {
    val nomeLimpo = form.nome.trim()
    val cepDigitos = form.cep.filter { it.isDigit() }
    return PerfilFormErros(
        nome =
            when {
                nomeLimpo.isEmpty() -> "O nome é obrigatório."
                nomeLimpo.length < Validators.NOME_ITEM_MIN ->
                    "O nome deve ter no mínimo ${Validators.NOME_ITEM_MIN} caracteres."
                nomeLimpo.length > 100 -> "O nome pode ter no máximo 100 caracteres."
                else -> null
            },
        cep =
            if (cepDigitos.isNotEmpty() && cepDigitos.length != Validators.CEP_DIGITOS) {
                "O CEP deve conter ${Validators.CEP_DIGITOS} dígitos."
            } else {
                null
            },
        logradouro =
            if (form.logradouro.trim().length > 100) {
                "O logradouro pode ter no máximo 100 caracteres."
            } else {
                null
            },
        numero =
            if (form.numero.trim().length > 10) {
                "O número pode ter no máximo 10 caracteres."
            } else {
                null
            },
        bairro =
            if (form.bairro.trim().length > 100) {
                "O bairro pode ter no máximo 100 caracteres."
            } else {
                null
            },
    )
}

/** Estado do formulário de alteração de senha. */
data class AlterarSenhaForm(
    val senhaAtual: String = "",
    val novaSenha: String = "",
    val confirmarNovaSenha: String = "",
)

/** Erros derivados do [AlterarSenhaForm]; `null` significa campo válido. */
data class AlterarSenhaFormErros(
    val senhaAtual: String? = null,
    val novaSenha: String? = null,
    val confirmarNovaSenha: String? = null,
) {
    val valido: Boolean
        get() =
            senhaAtual == null &&
                novaSenha == null &&
                confirmarNovaSenha == null
}

/** Validação pura do formulário de alteração de senha. */
fun validarAlterarSenha(form: AlterarSenhaForm): AlterarSenhaFormErros {
    return AlterarSenhaFormErros(
        senhaAtual =
            if (form.senhaAtual.isBlank()) {
                "Informe a sua senha atual."
            } else {
                null
            },
        novaSenha =
            when {
                form.novaSenha.isEmpty() -> "Informe a nova senha."
                form.novaSenha.length < Validators.SENHA_MIN ->
                    "A nova senha deve ter pelo menos ${Validators.SENHA_MIN} caracteres."
                form.senhaAtual.isNotEmpty() && form.novaSenha == form.senhaAtual ->
                    "A nova senha deve ser diferente da senha atual."
                else -> null
            },
        confirmarNovaSenha =
            when {
                form.confirmarNovaSenha.isEmpty() -> "Confirme a nova senha."
                form.confirmarNovaSenha != form.novaSenha -> "A confirmação não coincide com a nova senha."
                else -> null
            },
    )
}
