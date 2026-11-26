package io.solidar.donation.domain.repository

import io.solidar.donation.domain.model.EfetivarDoacaoInput
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.model.Interessado

interface DoacaoRepository {
    suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray? = null,
        imagemNome: String? = null,
    ): Result<Doacao>

    suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>>

    suspend fun listarDoacoesDisponiveis(
        categoria: String? = null,
        busca: String? = null,
    ): Result<ResultadoCacheado<List<DoacaoDisponivel>>>

    suspend fun registrarInteresse(doacaoId: String): Result<Unit>

    suspend fun registrarInteresse(
        doacaoId: String,
        mensagem: String?,
    ): Result<Unit> = registrarInteresse(doacaoId)

    suspend fun removerInteresse(doacaoId: String): Result<Unit>

    fun listarInteressesIds(): Set<String>

    suspend fun listarInteressados(doacaoId: String): Result<List<Interessado>> = Result.success(emptyList())

    suspend fun efetivarDoacao(
        doacaoId: String,
        input: EfetivarDoacaoInput,
    ): Result<Doacao> = Result.failure(UnsupportedOperationException("Não implementado"))

    suspend fun cancelarDoacao(doacaoId: String): Result<Unit> = Result.failure(UnsupportedOperationException("Não implementado"))

    suspend fun doarItensParaCampanha(
        campanhaId: String,
        campanhaTitulo: String,
        itens: List<NovaDoacaoInput>,
        itensIds: Set<String>,
        donatarioId: Int? = null,
        quantidadesPorItemId: Map<String, Int> = emptyMap(),
    ): Result<Doacao> =
        if (itens.isEmpty()) {
            Result.failure(IllegalArgumentException("Selecione ao menos um item."))
        } else {
            criarDoacao(itens.first().copy(donatarioId = donatarioId))
        }

    fun listarItensDoadosCampanha(campanhaId: String): Set<String> = emptySet()

    fun listarQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> = emptyMap()
}
