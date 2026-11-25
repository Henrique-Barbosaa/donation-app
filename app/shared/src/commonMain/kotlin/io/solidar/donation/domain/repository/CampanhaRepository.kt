package io.solidar.donation.domain.repository

import io.solidar.donation.domain.model.NovaCampanhaInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.model.Campaign

interface CampanhaRepository {
    suspend fun listarCampanhas(): Result<ResultadoCacheado<List<Campaign>>>

    suspend fun obterCampanhaPorId(id: String): Result<Campaign?>

    suspend fun criarCampanha(input: NovaCampanhaInput): Result<Campaign>

    suspend fun listarMinhasCampanhas(): Result<ResultadoCacheado<List<Campaign>>> = Result.success(ResultadoCacheado(emptyList()))

    suspend fun cancelarCampanha(id: String): Result<Unit> = Result.failure(UnsupportedOperationException("Não implementado"))

    suspend fun finalizarCampanha(id: String): Result<Unit> = Result.failure(UnsupportedOperationException("Não implementado"))

    fun listarFavoritosIds(): Set<String>

    fun alternarFavorito(campanhaId: String): Set<String>
}
