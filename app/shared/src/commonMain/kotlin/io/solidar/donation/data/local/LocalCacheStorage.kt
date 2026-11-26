package io.solidar.donation.data.local

import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.CampaignLocalColeta
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.readPlatformFileText
import io.solidar.donation.writePlatformFileText
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Contrato de persistência local para que os dados do usuário sobrevivam ao fechamento do app
 * e possam ser exibidos sem rede marcados como desatualizados.
 */
interface LocalCacheStorage {
    fun salvarCampanhas(campanhas: List<Campaign>)

    fun lerCampanhas(): List<Campaign>?

    fun salvarDoacoes(doacoes: List<Doacao>)

    fun lerDoacoes(): List<Doacao>?

    fun salvarDoacoesUsuario(
        usuarioChave: String,
        doacoes: List<Doacao>,
    ) {
        salvarDoacoes(doacoes)
    }

    fun lerDoacoesUsuario(usuarioChave: String): List<Doacao>? = lerDoacoes()

    fun salvarMinhasCampanhasUsuario(
        usuarioChave: String,
        campanhas: List<Campaign>,
    ) {}

    fun lerMinhasCampanhasUsuario(usuarioChave: String): List<Campaign>? = null

    fun salvarDoacoesDisponiveis(doacoes: List<DoacaoDisponivel>) {}

    fun lerDoacoesDisponiveis(): List<DoacaoDisponivel>? = null

    fun salvarFavoritos(ids: Set<String>)

    fun lerFavoritos(): Set<String>

    fun salvarFavoritosUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        salvarFavoritos(ids)
    }

    fun lerFavoritosUsuario(usuarioChave: String): Set<String> = lerFavoritos()

    fun salvarInteresses(ids: Set<String>) {}

    fun lerInteresses(): Set<String> = emptySet()

    fun salvarInteressesUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        salvarInteresses(ids)
    }

    fun lerInteressesUsuario(usuarioChave: String): Set<String> = lerInteresses()

    fun salvarItensDoadosCampanha(
        campanhaId: String,
        itensIds: Set<String>,
    ) {}

    fun lerItensDoadosCampanha(campanhaId: String): Set<String> = emptySet()

    fun salvarQuantidadesDoadasCampanha(
        campanhaId: String,
        quantidades: Map<String, Int>,
    ) {}

    fun lerQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> = emptyMap()
}

@Serializable
internal data class CampaignItemCacheDto(
    val id: String,
    val nome: String,
    val quantidadeDescricao: String,
    val iconeRef: String,
    val quantidadeAlvo: Int? = null,
)

@Serializable
internal data class CampaignLocalColetaCacheDto(
    val nome: String,
    val enderecoResumo: String = "",
)

@Serializable
internal data class CampaignCacheDto(
    val id: String,
    val title: String,
    val organizer: String,
    val goalInCents: Long,
    val raisedInCents: Long,
    val descricao: String = "",
    val imageUrl: String? = null,
    val itensNecessarios: List<CampaignItemCacheDto> = emptyList(),
    val locaisColeta: List<CampaignLocalColetaCacheDto> = emptyList(),
    val instituicaoId: Int? = null,
    val status: String = "ATIVA",
)

@Serializable
internal data class DoacaoCacheDto(
    val id: String,
    val itemTitle: String,
    val counterpartName: String,
    val direction: String,
    val status: String,
    val dateLabel: String,
)

@Serializable
internal data class DoacaoDisponivelCacheDto(
    val id: String,
    val itemTitle: String,
    val quantidade: Int,
    val categoria: String,
    val condicao: String,
    val descricao: String = "",
    val motivo: String = "",
    val nomeDoador: String = "Doador Solidário",
    val urlImagem: String? = null,
    val doadorId: Int? = null,
)

/**
 * Implementação persistente em disco que grava arquivos JSON na pasta de arquivos de cada
 * plataforma (`filesDir` no Android, diretório do usuário na JVM, `NSUserDefaults`/Documents no iOS).
 */
class FileLocalCacheStorage(
    private val json: Json = HttpClientFactory.defaultJson,
    private val reader: (String) -> String? = ::readPlatformFileText,
    private val writer: (String, String) -> Unit = ::writePlatformFileText,
) : LocalCacheStorage {
    override fun salvarCampanhas(campanhas: List<Campaign>) {
        val dtos = campanhas.map { it.toCacheDto() }
        runCatching {
            writer(ARQUIVO_CAMPANHAS, json.encodeToString(dtos))
        }
    }

    override fun lerCampanhas(): List<Campaign>? {
        val raw = reader(ARQUIVO_CAMPANHAS) ?: return null
        return runCatching {
            json.decodeFromString<List<CampaignCacheDto>>(raw).map { it.toDomain() }
        }.getOrNull()
    }

    override fun salvarDoacoes(doacoes: List<Doacao>) {
        val dtos =
            doacoes.map { doacao ->
                DoacaoCacheDto(
                    id = doacao.id,
                    itemTitle = doacao.itemTitle,
                    counterpartName = doacao.counterpartName,
                    direction = doacao.direction.name,
                    status = doacao.status.name,
                    dateLabel = doacao.dateLabel,
                )
            }
        runCatching {
            writer(ARQUIVO_DOACOES, json.encodeToString(dtos))
        }
    }

    override fun lerDoacoes(): List<Doacao>? {
        val raw = reader(ARQUIVO_DOACOES) ?: return null
        return runCatching {
            json.decodeFromString<List<DoacaoCacheDto>>(raw).map { it.toDomain() }
        }.getOrNull()
    }

    override fun salvarDoacoesUsuario(
        usuarioChave: String,
        doacoes: List<Doacao>,
    ) {
        val mapa = lerMapaDoacoes().toMutableMap()
        mapa[usuarioChave] = doacoes.map { it.toCacheDto() }
        runCatching {
            writer(ARQUIVO_DOACOES_USUARIO, json.encodeToString(mapa))
        }
    }

    override fun lerDoacoesUsuario(usuarioChave: String): List<Doacao>? = lerMapaDoacoes()[usuarioChave]?.map { it.toDomain() }

    override fun salvarMinhasCampanhasUsuario(
        usuarioChave: String,
        campanhas: List<Campaign>,
    ) {
        val mapa = lerMapaMinhasCampanhas().toMutableMap()
        mapa[usuarioChave] = campanhas.map { it.toCacheDto() }
        runCatching {
            writer(ARQUIVO_MINHAS_CAMPANHAS_USUARIO, json.encodeToString(mapa))
        }
    }

    override fun lerMinhasCampanhasUsuario(usuarioChave: String): List<Campaign>? =
        lerMapaMinhasCampanhas()[usuarioChave]?.map { it.toDomain() }

    override fun salvarDoacoesDisponiveis(doacoes: List<DoacaoDisponivel>) {
        val dtos =
            doacoes.map { item ->
                DoacaoDisponivelCacheDto(
                    id = item.id,
                    itemTitle = item.itemTitle,
                    quantidade = item.quantidade,
                    categoria = item.categoria,
                    condicao = item.condicao,
                    descricao = item.descricao,
                    motivo = item.motivo,
                    nomeDoador = item.nomeDoador,
                    urlImagem = item.urlImagem,
                    doadorId = item.doadorId,
                )
            }
        runCatching {
            writer(ARQUIVO_DOACOES_DISPONIVEIS, json.encodeToString(dtos))
        }
    }

    override fun lerDoacoesDisponiveis(): List<DoacaoDisponivel>? {
        val raw = reader(ARQUIVO_DOACOES_DISPONIVEIS) ?: return null
        return runCatching {
            json.decodeFromString<List<DoacaoDisponivelCacheDto>>(raw).map { dto ->
                DoacaoDisponivel(
                    id = dto.id,
                    itemTitle = dto.itemTitle,
                    quantidade = dto.quantidade,
                    categoria = dto.categoria,
                    condicao = dto.condicao,
                    descricao = dto.descricao,
                    motivo = dto.motivo,
                    nomeDoador = dto.nomeDoador,
                    urlImagem = dto.urlImagem,
                    doadorId = dto.doadorId,
                )
            }
        }.getOrNull()
    }

    override fun salvarFavoritos(ids: Set<String>) {
        runCatching {
            writer(ARQUIVO_FAVORITOS, json.encodeToString(ids.toList()))
        }
    }

    override fun lerFavoritos(): Set<String> {
        val raw = reader(ARQUIVO_FAVORITOS) ?: return emptySet()
        return runCatching {
            json.decodeFromString<List<String>>(raw).toSet()
        }.getOrDefault(emptySet())
    }

    override fun salvarFavoritosUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        val mapa = lerMapaStrings(ARQUIVO_FAVORITOS_USUARIO).toMutableMap()
        mapa[usuarioChave] = ids.toList()
        runCatching {
            writer(ARQUIVO_FAVORITOS_USUARIO, json.encodeToString(mapa))
        }
    }

    override fun lerFavoritosUsuario(usuarioChave: String): Set<String> =
        lerMapaStrings(ARQUIVO_FAVORITOS_USUARIO)[usuarioChave]?.toSet().orEmpty()

    override fun salvarInteresses(ids: Set<String>) {
        runCatching {
            writer(ARQUIVO_INTERESSES, json.encodeToString(ids.toList()))
        }
    }

    override fun lerInteresses(): Set<String> {
        val raw = reader(ARQUIVO_INTERESSES) ?: return emptySet()
        return runCatching {
            json.decodeFromString<List<String>>(raw).toSet()
        }.getOrDefault(emptySet())
    }

    override fun salvarInteressesUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        val mapa = lerMapaStrings(ARQUIVO_INTERESSES_USUARIO).toMutableMap()
        mapa[usuarioChave] = ids.toList()
        runCatching {
            writer(ARQUIVO_INTERESSES_USUARIO, json.encodeToString(mapa))
        }
    }

    override fun lerInteressesUsuario(usuarioChave: String): Set<String> =
        lerMapaStrings(ARQUIVO_INTERESSES_USUARIO)[usuarioChave]?.toSet().orEmpty()

    override fun salvarItensDoadosCampanha(
        campanhaId: String,
        itensIds: Set<String>,
    ) {
        val mapaAtual = lerMapaStrings(ARQUIVO_ITENS_DOADOS).toMutableMap()
        mapaAtual[campanhaId] = itensIds.toList()
        runCatching {
            writer(ARQUIVO_ITENS_DOADOS, json.encodeToString(mapaAtual))
        }
    }

    override fun lerItensDoadosCampanha(campanhaId: String): Set<String> =
        lerMapaStrings(ARQUIVO_ITENS_DOADOS)[campanhaId]?.toSet().orEmpty()

    override fun salvarQuantidadesDoadasCampanha(
        campanhaId: String,
        quantidades: Map<String, Int>,
    ) {
        val mapaAtual = lerMapaQuantidades().toMutableMap()
        mapaAtual[campanhaId] = quantidades
        runCatching {
            writer(ARQUIVO_QUANTIDADES_DOADAS, json.encodeToString(mapaAtual))
        }
    }

    override fun lerQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> = lerMapaQuantidades()[campanhaId].orEmpty()

    private fun lerMapaStrings(arquivo: String): Map<String, List<String>> {
        val raw = reader(arquivo) ?: return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, List<String>>>(raw)
        }.getOrDefault(emptyMap())
    }

    private fun lerMapaQuantidades(): Map<String, Map<String, Int>> {
        val raw = reader(ARQUIVO_QUANTIDADES_DOADAS) ?: return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, Map<String, Int>>>(raw)
        }.getOrDefault(emptyMap())
    }

    private fun lerMapaDoacoes(): Map<String, List<DoacaoCacheDto>> {
        val raw = reader(ARQUIVO_DOACOES_USUARIO) ?: return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, List<DoacaoCacheDto>>>(raw)
        }.getOrDefault(emptyMap())
    }

    private fun lerMapaMinhasCampanhas(): Map<String, List<CampaignCacheDto>> {
        val raw = reader(ARQUIVO_MINHAS_CAMPANHAS_USUARIO) ?: return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, List<CampaignCacheDto>>>(raw)
        }.getOrDefault(emptyMap())
    }

    private fun Doacao.toCacheDto(): DoacaoCacheDto =
        DoacaoCacheDto(
            id = id,
            itemTitle = itemTitle,
            counterpartName = counterpartName,
            direction = direction.name,
            status = status.name,
            dateLabel = dateLabel,
        )

    private fun DoacaoCacheDto.toDomain(): Doacao =
        Doacao(
            id = id,
            itemTitle = itemTitle,
            counterpartName = counterpartName,
            direction =
                runCatching { DoacaoDirecao.valueOf(direction) }
                    .getOrDefault(DoacaoDirecao.FEITA),
            status =
                runCatching { DoacaoStatus.valueOf(status) }
                    .getOrDefault(DoacaoStatus.PENDENTE),
            dateLabel = dateLabel,
        )

    private fun Campaign.toCacheDto(): CampaignCacheDto =
        CampaignCacheDto(
            id = id,
            title = title,
            organizer = organizer,
            goalInCents = goalInCents,
            raisedInCents = raisedInCents,
            descricao = descricao,
            imageUrl = imageUrl,
            instituicaoId = instituicaoId,
            status = status,
            itensNecessarios =
                itensNecessarios.map { item ->
                    CampaignItemCacheDto(
                        id = item.id,
                        nome = item.nome,
                        quantidadeDescricao = item.quantidadeDescricao,
                        iconeRef = item.iconeRef,
                        quantidadeAlvo = item.quantidadeAlvo,
                    )
                },
            locaisColeta =
                locaisColeta.map { local ->
                    CampaignLocalColetaCacheDto(
                        nome = local.nome,
                        enderecoResumo = local.enderecoResumo,
                    )
                },
        )

    private fun CampaignCacheDto.toDomain(): Campaign =
        Campaign(
            id = id,
            title = title,
            organizer = organizer,
            goalInCents = goalInCents,
            raisedInCents = raisedInCents,
            descricao = descricao,
            imageUrl = imageUrl,
            instituicaoId = instituicaoId,
            status = status,
            itensNecessarios =
                itensNecessarios.map { item ->
                    CampaignItem(
                        id = item.id,
                        nome = item.nome,
                        quantidadeDescricao = item.quantidadeDescricao,
                        iconeRef = item.iconeRef,
                        quantidadeAlvo = item.quantidadeAlvo,
                    )
                },
            locaisColeta =
                locaisColeta.map { local ->
                    CampaignLocalColeta(
                        nome = local.nome,
                        enderecoResumo = local.enderecoResumo,
                    )
                },
        )

    companion object {
        const val ARQUIVO_CAMPANHAS = "campanhas_cache.json"
        const val ARQUIVO_DOACOES = "doacoes_cache.json"
        const val ARQUIVO_DOACOES_USUARIO = "doacoes_por_usuario_cache.json"
        const val ARQUIVO_MINHAS_CAMPANHAS_USUARIO = "minhas_campanhas_por_usuario_cache.json"
        const val ARQUIVO_DOACOES_DISPONIVEIS = "doacoes_disponiveis_cache.json"
        const val ARQUIVO_FAVORITOS = "favoritos_cache.json"
        const val ARQUIVO_FAVORITOS_USUARIO = "favoritos_por_usuario_cache.json"
        const val ARQUIVO_INTERESSES = "interesses_cache.json"
        const val ARQUIVO_INTERESSES_USUARIO = "interesses_por_usuario_cache.json"
        const val ARQUIVO_ITENS_DOADOS = "itens_doados_campanha_cache.json"
        const val ARQUIVO_QUANTIDADES_DOADAS = "quantidades_doadas_campanha_cache.json"
    }
}

/** Implementação em memória para testes unitários determinísticos. */
class InMemoryLocalCacheStorage : LocalCacheStorage {
    private var campanhas: List<Campaign>? = null
    private var doacoes: List<Doacao>? = null
    private val doacoesPorUsuario = mutableMapOf<String, List<Doacao>>()
    private val minhasCampanhasPorUsuario = mutableMapOf<String, List<Campaign>>()
    private var doacoesDisponiveis: List<DoacaoDisponivel>? = null
    private var favoritos: Set<String> = emptySet()
    private val favoritosPorUsuario = mutableMapOf<String, Set<String>>()
    private var interesses: Set<String> = emptySet()
    private val interessesPorUsuario = mutableMapOf<String, Set<String>>()
    private val itensDoadosPorCampanha = mutableMapOf<String, Set<String>>()
    private val quantidadesDoadasPorCampanha = mutableMapOf<String, Map<String, Int>>()

    override fun salvarCampanhas(campanhas: List<Campaign>) {
        this.campanhas = campanhas
    }

    override fun lerCampanhas(): List<Campaign>? = campanhas

    override fun salvarDoacoes(doacoes: List<Doacao>) {
        this.doacoes = doacoes
    }

    override fun lerDoacoes(): List<Doacao>? = doacoes

    override fun salvarDoacoesUsuario(
        usuarioChave: String,
        doacoes: List<Doacao>,
    ) {
        doacoesPorUsuario[usuarioChave] = doacoes
    }

    override fun lerDoacoesUsuario(usuarioChave: String): List<Doacao>? = doacoesPorUsuario[usuarioChave]

    override fun salvarMinhasCampanhasUsuario(
        usuarioChave: String,
        campanhas: List<Campaign>,
    ) {
        minhasCampanhasPorUsuario[usuarioChave] = campanhas
    }

    override fun lerMinhasCampanhasUsuario(usuarioChave: String): List<Campaign>? = minhasCampanhasPorUsuario[usuarioChave]

    override fun salvarDoacoesDisponiveis(doacoes: List<DoacaoDisponivel>) {
        this.doacoesDisponiveis = doacoes
    }

    override fun lerDoacoesDisponiveis(): List<DoacaoDisponivel>? = doacoesDisponiveis

    override fun salvarFavoritos(ids: Set<String>) {
        this.favoritos = ids
    }

    override fun lerFavoritos(): Set<String> = favoritos

    override fun salvarFavoritosUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        favoritosPorUsuario[usuarioChave] = ids
    }

    override fun lerFavoritosUsuario(usuarioChave: String): Set<String> = favoritosPorUsuario[usuarioChave].orEmpty()

    override fun salvarInteresses(ids: Set<String>) {
        this.interesses = ids
    }

    override fun lerInteresses(): Set<String> = interesses

    override fun salvarInteressesUsuario(
        usuarioChave: String,
        ids: Set<String>,
    ) {
        interessesPorUsuario[usuarioChave] = ids
    }

    override fun lerInteressesUsuario(usuarioChave: String): Set<String> = interessesPorUsuario[usuarioChave].orEmpty()

    override fun salvarItensDoadosCampanha(
        campanhaId: String,
        itensIds: Set<String>,
    ) {
        itensDoadosPorCampanha[campanhaId] = itensIds
    }

    override fun lerItensDoadosCampanha(campanhaId: String): Set<String> = itensDoadosPorCampanha[campanhaId].orEmpty()

    override fun salvarQuantidadesDoadasCampanha(
        campanhaId: String,
        quantidades: Map<String, Int>,
    ) {
        quantidadesDoadasPorCampanha[campanhaId] = quantidades
    }

    override fun lerQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> = quantidadesDoadasPorCampanha[campanhaId].orEmpty()
}
