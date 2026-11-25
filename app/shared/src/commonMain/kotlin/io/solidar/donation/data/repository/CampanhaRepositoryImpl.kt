package io.solidar.donation.data.repository

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.CampanhaRemoteDataSource
import io.solidar.donation.data.remote.NetworkErrorMapper
import io.solidar.donation.data.remote.dto.toCriarCampanhaRequest
import io.solidar.donation.data.remote.dto.toDomain
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.NovaCampanhaInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.model.Campaign

class CampanhaRepositoryImpl(
    private val remoteDataSource: CampanhaRemoteDataSource = CampanhaRemoteDataSource(),
    private val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
    private val tokenStorage: TokenStorage = SessionStorage.instance,
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

    override suspend fun criarCampanha(input: NovaCampanhaInput): Result<Campaign> {
        return try {
            val criada = remoteDataSource.criarCampanha(input.toCriarCampanhaRequest()).toDomain()
            val atuais = localCacheStorage.lerCampanhas().orEmpty()
            localCacheStorage.salvarCampanhas(listOf(criada) + atuais.filterNot { it.id == criada.id })
            Result.success(criada)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun listarMinhasCampanhas(): Result<ResultadoCacheado<List<Campaign>>> {
        return try {
            val minhas = remoteDataSource.listarMinhasCampanhas().map { it.toDomain() }
            Result.success(ResultadoCacheado(dados = minhas, desatualizado = false))
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun finalizarCampanha(id: String): Result<Unit> {
        return try {
            remoteDataSource.finalizarCampanha(id)
            localCacheStorage.lerCampanhas()?.let { atuais ->
                localCacheStorage.salvarCampanhas(atuais.filterNot { it.id == id })
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun cancelarCampanha(id: String): Result<Unit> {
        return try {
            remoteDataSource.cancelarCampanha(id)
            localCacheStorage.lerCampanhas()?.let { atuais ->
                localCacheStorage.salvarCampanhas(atuais.filterNot { it.id == id })
            }
            val chaveUsuario = tokenStorage.getSession()?.id?.toString()
            if (chaveUsuario != null) {
                val favs = localCacheStorage.lerFavoritosUsuario(chaveUsuario) - id
                localCacheStorage.salvarFavoritosUsuario(chaveUsuario, favs)
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override fun listarFavoritosIds(): Set<String> {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        return if (chaveUsuario != null) {
            localCacheStorage.lerFavoritosUsuario(chaveUsuario)
        } else {
            localCacheStorage.lerFavoritos()
        }
    }

    override fun alternarFavorito(campanhaId: String): Set<String> {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        val atuais = listarFavoritosIds().toMutableSet()
        if (atuais.contains(campanhaId)) {
            atuais.remove(campanhaId)
        } else {
            atuais.add(campanhaId)
        }
        if (chaveUsuario != null) {
            localCacheStorage.salvarFavoritosUsuario(chaveUsuario, atuais)
        } else {
            localCacheStorage.salvarFavoritos(atuais)
        }
        return atuais
    }
}
