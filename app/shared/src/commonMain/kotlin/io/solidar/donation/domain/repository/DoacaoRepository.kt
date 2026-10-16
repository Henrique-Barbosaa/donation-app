package io.solidar.donation.domain.repository

import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse

interface DoacaoRepository {
    suspend fun criarDoacao(
        request: CriarDoacaoRequest,
        imagemBytes: ByteArray? = null,
        imagemNome: String? = null,
    ): Result<DoacaoResponse>
}
