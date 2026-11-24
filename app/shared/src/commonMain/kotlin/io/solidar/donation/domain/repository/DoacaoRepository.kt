package io.solidar.donation.domain.repository

import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.model.Doacao

interface DoacaoRepository {
    suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray? = null,
        imagemNome: String? = null,
    ): Result<Doacao>

    suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>>
}
