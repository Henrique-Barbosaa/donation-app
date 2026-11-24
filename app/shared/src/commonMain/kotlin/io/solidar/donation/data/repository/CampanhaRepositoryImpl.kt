package io.solidar.donation.data.repository

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.CampanhaRemoteDataSource
import io.solidar.donation.data.remote.NetworkErrorMapper
import io.solidar.donation.data.remote.dto.toDomain
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.model.Campaign

class CampanhaRepositoryImpl(
    private val remoteDataSource: CampanhaRemoteDataSource = CampanhaRemoteDataSource(),
    private val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
) : CampanhaRepository {
    override suspend fun listarCampanhas(): Result<ResultadoCacheado<List<Campaign>>> {
        return try {
            val campanhas = remoteDataSource.listarCampanhas().map { it.toDomain() }
            localCacheStorage.salvarCampanhas(campanhas)
            Result.success(ResultadoCacheado(dados = campanhas, desatualizado = false))
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao) {
                val cache = localCacheStorage.lerCampanhas()
                if (cache != null) {
                    return Result.success(ResultadoCacheado(dados = cache, desatualizado = true))
                }
            }
            Result.failure(falha)
        }
    }

    override suspend fun obterCampanhaPorId(id: String): Result<Campaign?> {
        val cached = localCacheStorage.lerCampanhas()?.firstOrNull { it.id == id }
        return try {
            val campanha = remoteDataSource.obterCampanhaPorId(id).toDomain()
            Result.success(campanha)
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao && cached != null) {
                Result.success(cached)
            } else {
                Result.failure(falha)
            }
        }
    }

    override fun listarFavoritosIds(): Set<String> = localCacheStorage.lerFavoritos()

    override fun alternarFavorito(campanhaId: String): Set<String> {
        val atuais = localCacheStorage.lerFavoritos().toMutableSet()
        if (atuais.contains(campanhaId)) {
            atuais.remove(campanhaId)
        } else {
            atuais.add(campanhaId)
        }
        localCacheStorage.salvarFavoritos(atuais)
        return atuais
    }
}
