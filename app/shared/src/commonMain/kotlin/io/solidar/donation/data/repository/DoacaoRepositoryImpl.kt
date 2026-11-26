package io.solidar.donation.data.repository

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.NetworkErrorMapper
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.ItemRequest
import io.solidar.donation.data.remote.dto.toCriarDoacaoRequest
import io.solidar.donation.data.remote.dto.toDisponivelDomain
import io.solidar.donation.data.remote.dto.toDomain
import io.solidar.donation.data.remote.dto.toEfetivarRequest
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.EfetivarDoacaoInput
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.model.Interessado

class DoacaoRepositoryImpl(
    private val tokenStorage: TokenStorage = SessionStorage.instance,
    private val remoteDataSource: DoacaoRemoteDataSource =
        DoacaoRemoteDataSource(
            httpClient = HttpClientFactory.create { tokenStorage.getToken() },
        ),
    private val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
) : DoacaoRepository {
    override suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray?,
        imagemNome: String?,
    ): Result<Doacao> {
        return try {
            val response =
                remoteDataSource.criarDoacao(
                    request = input.toCriarDoacaoRequest(),
                    imagemBytes = imagemBytes,
                    imagemNome = imagemNome,
                )
            val doacao = response.toDomain(DoacaoDirecao.FEITA)
            val atuais = lerDoacoesEscopadas().orEmpty()
            salvarDoacoesEscopadas(listOf(doacao) + atuais)
            Result.success(doacao)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>> {
        return try {
            val feitas = remoteDataSource.listarMinhasDoacoes().map { it.toDomain(DoacaoDirecao.FEITA) }
            val recebidas = remoteDataSource.listarDoacoesRecebidas().map { it.toDomain(DoacaoDirecao.RECEBIDA) }
            val todas = feitas + recebidas
            salvarDoacoesEscopadas(todas)
            Result.success(ResultadoCacheado(dados = todas, desatualizado = false))
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao) {
                val cache = lerDoacoesEscopadas()
                if (cache != null) {
                    return Result.success(ResultadoCacheado(dados = cache, desatualizado = true))
                }
            }
            Result.failure(falha)
        }
    }

    override suspend fun listarDoacoesDisponiveis(
        categoria: String?,
        busca: String?,
    ): Result<ResultadoCacheado<List<DoacaoDisponivel>>> {
        return try {
            val disponiveis =
                remoteDataSource
                    .listarDoacoesDisponiveis(categoria = categoria, busca = busca)
                    .map { it.toDisponivelDomain() }
            if (categoria.isNullOrBlank() && busca.isNullOrBlank()) {
                localCacheStorage.salvarDoacoesDisponiveis(disponiveis)
            }
            Result.success(ResultadoCacheado(dados = disponiveis, desatualizado = false))
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao) {
                val cache = localCacheStorage.lerDoacoesDisponiveis()
                if (cache != null) {
                    val filtrados = filtrarLocalmente(cache, categoria, busca)
                    return Result.success(ResultadoCacheado(dados = filtrados, desatualizado = true))
                }
            }
            Result.failure(falha)
        }
    }

    override suspend fun registrarInteresse(doacaoId: String): Result<Unit> = registrarInteresse(doacaoId, null)

    override suspend fun registrarInteresse(
        doacaoId: String,
        mensagem: String?,
    ): Result<Unit> {
        return try {
            remoteDataSource.registrarInteresse(doacaoId, mensagem)
            val atualizados = listarInteressesIds() + doacaoId
            salvarInteressesEscopados(atualizados)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun removerInteresse(doacaoId: String): Result<Unit> {
        return try {
            remoteDataSource.removerInteresse(doacaoId)
            val atualizados = listarInteressesIds() - doacaoId
            salvarInteressesEscopados(atualizados)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override fun listarInteressesIds(): Set<String> {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        return if (chaveUsuario != null) {
            localCacheStorage.lerInteressesUsuario(chaveUsuario)
        } else {
            localCacheStorage.lerInteresses()
        }
    }

    private fun salvarInteressesEscopados(ids: Set<String>) {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        if (chaveUsuario != null) {
            localCacheStorage.salvarInteressesUsuario(chaveUsuario, ids)
        } else {
            localCacheStorage.salvarInteresses(ids)
        }
    }

    override suspend fun listarInteressados(doacaoId: String): Result<List<Interessado>> {
        return try {
            val interessados = remoteDataSource.listarInteressados(doacaoId).map { it.toDomain() }
            Result.success(interessados)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun efetivarDoacao(
        doacaoId: String,
        input: EfetivarDoacaoInput,
    ): Result<Doacao> {
        return try {
            val response = remoteDataSource.efetivarDoacao(doacaoId, input.toEfetivarRequest())
            val efetivada = response.toDomain(DoacaoDirecao.FEITA)
            val historicoAtual = lerDoacoesEscopadas().orEmpty()
            salvarDoacoesEscopadas(
                historicoAtual.map { if (it.id == efetivada.id) efetivada else it },
            )
            localCacheStorage.lerDoacoesDisponiveis()?.let { disponiveis ->
                localCacheStorage.salvarDoacoesDisponiveis(disponiveis.filterNot { it.id == doacaoId })
            }
            Result.success(efetivada)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun cancelarDoacao(doacaoId: String): Result<Unit> {
        return try {
            remoteDataSource.cancelarDoacao(doacaoId)
            lerDoacoesEscopadas()?.let { historico ->
                salvarDoacoesEscopadas(historico.filterNot { it.id == doacaoId })
            }
            localCacheStorage.lerDoacoesDisponiveis()?.let { disponiveis ->
                localCacheStorage.salvarDoacoesDisponiveis(disponiveis.filterNot { it.id == doacaoId })
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun doarItensParaCampanha(
        campanhaId: String,
        campanhaTitulo: String,
        itens: List<NovaDoacaoInput>,
        itensIds: Set<String>,
        donatarioId: Int?,
        quantidadesPorItemId: Map<String, Int>,
    ): Result<Doacao> {
        return try {
            val request =
                CriarDoacaoRequest(
                    itens =
                        itens.map { item ->
                            ItemRequest(
                                nome = item.nome.trim().take(45),
                                quantidade = item.quantidade.coerceAtLeast(1),
                                descricao = item.descricao.trim().ifBlank { null },
                                motivo = "Campanha: $campanhaTitulo".take(200),
                                eNovo = if (item.eNovo) "S" else "N",
                                categoria = item.categoria.uppercase(),
                            )
                        },
                    donatarioId = donatarioId,
                )
            val response = remoteDataSource.criarDoacao(request = request)
            val doacao = response.toDomain(DoacaoDirecao.FEITA)
            val atuais = lerDoacoesEscopadas().orEmpty()
            salvarDoacoesEscopadas(listOf(doacao) + atuais)
            val jaDoados = localCacheStorage.lerItensDoadosCampanha(campanhaId)
            localCacheStorage.salvarItensDoadosCampanha(campanhaId, jaDoados + itensIds)
            if (quantidadesPorItemId.isNotEmpty()) {
                val qtdAtuais = localCacheStorage.lerQuantidadesDoadasCampanha(campanhaId).toMutableMap()
                quantidadesPorItemId.forEach { (itemId, qtd) ->
                    qtdAtuais[itemId] = (qtdAtuais[itemId] ?: 0) + qtd.coerceAtLeast(1)
                }
                localCacheStorage.salvarQuantidadesDoadasCampanha(campanhaId, qtdAtuais)
            }
            Result.success(doacao)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    private fun lerDoacoesEscopadas(): List<Doacao>? {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        return if (chaveUsuario != null) {
            localCacheStorage.lerDoacoesUsuario(chaveUsuario)
        } else {
            localCacheStorage.lerDoacoes()
        }
    }

    private fun salvarDoacoesEscopadas(doacoes: List<Doacao>) {
        val chaveUsuario = tokenStorage.getSession()?.id?.toString()
        if (chaveUsuario != null) {
            localCacheStorage.salvarDoacoesUsuario(chaveUsuario, doacoes)
        } else {
            localCacheStorage.salvarDoacoes(doacoes)
        }
    }

    override fun listarItensDoadosCampanha(campanhaId: String): Set<String> = localCacheStorage.lerItensDoadosCampanha(campanhaId)

    override fun listarQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> =
        localCacheStorage.lerQuantidadesDoadasCampanha(campanhaId)

    private fun filtrarLocalmente(
        doacoes: List<DoacaoDisponivel>,
        categoria: String?,
        busca: String?,
    ): List<DoacaoDisponivel> {
        val termo = busca?.trim().orEmpty()
        return doacoes.filter { item ->
            val matchCat = categoria.isNullOrBlank() || item.categoria.equals(categoria, ignoreCase = true)
            val matchBusca =
                termo.isEmpty() ||
                    item.itemTitle.contains(termo, ignoreCase = true) ||
                    item.descricao.contains(termo, ignoreCase = true) ||
                    item.nomeDoador.contains(termo, ignoreCase = true)
            matchCat && matchBusca
        }
    }
}
