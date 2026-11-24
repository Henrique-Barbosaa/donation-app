package io.solidar.donation.domain.repository

import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.model.Campaign

interface CampanhaRepository {
    suspend fun listarCampanhas(): Result<ResultadoCacheado<List<Campaign>>>

    suspend fun obterCampanhaPorId(id: String): Result<Campaign?>

    fun listarFavoritosIds(): Set<String>

    fun alternarFavorito(campanhaId: String): Set<String>
}
