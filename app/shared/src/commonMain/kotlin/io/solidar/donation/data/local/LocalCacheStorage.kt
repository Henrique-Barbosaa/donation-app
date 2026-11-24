package io.solidar.donation.data.local

import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
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

    fun salvarFavoritos(ids: Set<String>)

    fun lerFavoritos(): Set<String>
}

@Serializable
internal data class CampaignItemCacheDto(
    val id: String,
    val nome: String,
    val quantidadeDescricao: String,
    val iconeRef: String,
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
        val dtos =
            campanhas.map { camp ->
                CampaignCacheDto(
                    id = camp.id,
                    title = camp.title,
                    organizer = camp.organizer,
                    goalInCents = camp.goalInCents,
                    raisedInCents = camp.raisedInCents,
                    descricao = camp.descricao,
                    imageUrl = camp.imageUrl,
                    itensNecessarios =
                        camp.itensNecessarios.map { item ->
                            CampaignItemCacheDto(
                                id = item.id,
                                nome = item.nome,
                                quantidadeDescricao = item.quantidadeDescricao,
                                iconeRef = item.iconeRef,
                            )
                        },
                )
            }
        runCatching {
            writer(ARQUIVO_CAMPANHAS, json.encodeToString(dtos))
        }
    }

    override fun lerCampanhas(): List<Campaign>? {
        val raw = reader(ARQUIVO_CAMPANHAS) ?: return null
        return runCatching {
            json.decodeFromString<List<CampaignCacheDto>>(raw).map { dto ->
                Campaign(
                    id = dto.id,
                    title = dto.title,
                    organizer = dto.organizer,
                    goalInCents = dto.goalInCents,
                    raisedInCents = dto.raisedInCents,
                    descricao = dto.descricao,
                    imageUrl = dto.imageUrl,
                    itensNecessarios =
                        dto.itensNecessarios.map { item ->
                            CampaignItem(
                                id = item.id,
                                nome = item.nome,
                                quantidadeDescricao = item.quantidadeDescricao,
                                iconeRef = item.iconeRef,
                            )
                        },
                )
            }
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
            json.decodeFromString<List<DoacaoCacheDto>>(raw).map { dto ->
                Doacao(
                    id = dto.id,
                    itemTitle = dto.itemTitle,
                    counterpartName = dto.counterpartName,
                    direction =
                        runCatching { DoacaoDirecao.valueOf(dto.direction) }
                            .getOrDefault(DoacaoDirecao.FEITA),
                    status =
                        runCatching { DoacaoStatus.valueOf(dto.status) }
                            .getOrDefault(DoacaoStatus.PENDENTE),
                    dateLabel = dto.dateLabel,
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

    companion object {
        const val ARQUIVO_CAMPANHAS = "campanhas_cache.json"
        const val ARQUIVO_DOACOES = "doacoes_cache.json"
        const val ARQUIVO_FAVORITOS = "favoritos_cache.json"
    }
}

/** Implementação em memória para testes unitários determinísticos. */
class InMemoryLocalCacheStorage : LocalCacheStorage {
    private var campanhas: List<Campaign>? = null
    private var doacoes: List<Doacao>? = null
    private var favoritos: Set<String> = emptySet()

    override fun salvarCampanhas(campanhas: List<Campaign>) {
        this.campanhas = campanhas
    }

    override fun lerCampanhas(): List<Campaign>? = campanhas

    override fun salvarDoacoes(doacoes: List<Doacao>) {
        this.doacoes = doacoes
    }

    override fun lerDoacoes(): List<Doacao>? = doacoes

    override fun salvarFavoritos(ids: Set<String>) {
        this.favoritos = ids
    }

    override fun lerFavoritos(): Set<String> = favoritos
}
