package io.solidar.donation

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.InMemoryLocalCacheStorage
import io.solidar.donation.data.remote.CampanhaRemoteDataSource
import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.PerfilRemoteDataSource
import io.solidar.donation.data.repository.CampanhaRepositoryImpl
import io.solidar.donation.data.repository.DoacaoRepositoryImpl
import io.solidar.donation.data.repository.PerfilRepositoryImpl
import io.solidar.donation.data.storage.InMemoryTokenStorage
import io.solidar.donation.di.AppContainer
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.AtualizarPerfilInput
import io.solidar.donation.model.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RepositoryAndCacheTest {
    @Test
    fun appContainer_montaDependenciasEViewModelsCorretamente() {
        val container =
            AppContainer(
                tokenStorage = InMemoryTokenStorage(),
                localCacheStorage = InMemoryLocalCacheStorage(),
            )

        assertNotNull(container.authRepository)
        assertNotNull(container.campanhaRepository)
        assertNotNull(container.doacaoRepository)
        assertNotNull(container.perfilRepository)
        assertNotNull(container.createAuthViewModel())
        assertNotNull(container.createCampanhasViewModel())
        assertNotNull(container.createHistoricoViewModel())
        assertNotNull(container.createCriarDoacaoViewModel())
        assertNotNull(container.createPerfilViewModel())
    }

    @Test
    fun campanhaRepository_comConexao_converteDtoParaDominioESalvaNoCacheLocal() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                              "content": [
                                {
                                  "id": 10,
                                  "nomeInstituicao": "ONG Natal Solidário",
                                  "titulo": "Agasalhos de Inverno",
                                  "descricao": "Arrecadação de cobertores",
                                  "itensFoco": "Cobertores, Casacos",
                                  "metaVoluntarios": 30,
                                  "campoNovoIgnorado": "valor"
                                }
                              ],
                              "totalElements": 1,
                              "totalPages": 1,
                              "currentPage": 0
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val cache = InMemoryLocalCacheStorage()
            val repository =
                CampanhaRepositoryImpl(
                    remoteDataSource =
                        CampanhaRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = cache,
                )

            val resultado = repository.listarCampanhas().getOrThrow()

            assertFalse(resultado.desatualizado)
            assertEquals(1, resultado.dados.size)
            assertEquals("10", resultado.dados.first().id)
            assertEquals("Agasalhos de Inverno", resultado.dados.first().title)
            assertEquals("ONG Natal Solidário", resultado.dados.first().organizer)
            assertEquals(2, resultado.dados.first().itensNecessarios.size)

            val salvoNoCache = cache.lerCampanhas()
            assertNotNull(salvoNoCache)
            assertEquals("Agasalhos de Inverno", salvoNoCache.first().title)
        }

    @Test
    fun campanhaRepository_semConexaoComCacheSalvo_devolveDadosDesatualizados() =
        runTest {
            var online = true
            val mockEngine =
                MockEngine {
                    if (online) {
                        respond(
                            content =
                                """
                                [
                                  {
                                    "id": 7,
                                    "nomeInstituicao": "Instituto Esperança",
                                    "titulo": "Cestas Básicas",
                                    "descricao": "Apoio alimentar"
                                  }
                                ]
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        throw IllegalStateException("Connection refused")
                    }
                }

            val arquivosDisco = mutableMapOf<String, String>()
            val fileCache =
                FileLocalCacheStorage(
                    reader = { arquivosDisco[it] },
                    writer = { nome, conteudo -> arquivosDisco[nome] = conteudo },
                )
            val repository =
                CampanhaRepositoryImpl(
                    remoteDataSource =
                        CampanhaRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = fileCache,
                )

            // 1. Primeira leitura online guarda os dados no disco
            val onlineResult = repository.listarCampanhas().getOrThrow()
            assertFalse(onlineResult.desatualizado)
            assertEquals("Cestas Básicas", onlineResult.dados.first().title)

            // 2. Desliga a rede: deve abrir mostrando o último conteúdo marcado como desatualizado
            online = false
            val offlineResult = repository.listarCampanhas().getOrThrow()
            assertTrue(offlineResult.desatualizado)
            assertEquals(1, offlineResult.dados.size)
            assertEquals("Cestas Básicas", offlineResult.dados.first().title)
        }

    @Test
    fun campanhaRepository_semConexaoESemCache_retornaFalhaSemConexao() =
        runTest {
            val mockEngine =
                MockEngine {
                    throw IllegalStateException("Network unreachable")
                }

            val repository =
                CampanhaRepositoryImpl(
                    remoteDataSource =
                        CampanhaRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = InMemoryLocalCacheStorage(),
                )

            val result = repository.listarCampanhas()
            assertTrue(result.isFailure)
            assertIs<FalhaDominio.SemConexao>(result.exceptionOrNull())
        }

    @Test
    fun campanhaRepository_erro500OuJsonInvalido_retornaFalhaRespostaInvalida() =
        runTest {
            var retornarJsonQuebrado = false
            val mockEngine =
                MockEngine {
                    if (retornarJsonQuebrado) {
                        respond(
                            content = "{ json_invalido_sem_fechar",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        respond(
                            content = """{"erro": "Falha interna"}""",
                            status = HttpStatusCode.InternalServerError,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }

            val repository =
                CampanhaRepositoryImpl(
                    remoteDataSource =
                        CampanhaRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = InMemoryLocalCacheStorage(),
                )

            val erro500 = repository.listarCampanhas()
            assertTrue(erro500.isFailure)
            val falha500 = assertIs<FalhaDominio.RespostaInvalida>(erro500.exceptionOrNull())
            assertEquals(500, falha500.statusCode)

            retornarJsonQuebrado = true
            val erroJson = repository.listarCampanhas()
            assertTrue(erroJson.isFailure)
            assertIs<FalhaDominio.RespostaInvalida>(erroJson.exceptionOrNull())
        }

    @Test
    fun doacaoRepository_historicoComECacheOffline_funcionaCorretamente() =
        runTest {
            var online = true
            val mockEngine =
                MockEngine { request ->
                    if (!online) {
                        throw IllegalStateException("Backend offline")
                    }
                    val payload =
                        if (request.url.encodedPath.endsWith("/minhas")) {
                            """
                            [
                              {
                                "id": 1,
                                "nomeDonatario": "ONG Luz",
                                "donatarioId": 5,
                                "itens": [{"nome": "Cesta Básica", "quantidade": 2}]
                              }
                            ]
                            """.trimIndent()
                        } else {
                            """
                            [
                              {
                                "id": 2,
                                "nomeDoador": "João",
                                "itens": [{"nome": "Caderno", "quantidade": 1}]
                              }
                            ]
                            """.trimIndent()
                        }
                    respond(
                        content = payload,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val cache = InMemoryLocalCacheStorage()
            val repo =
                DoacaoRepositoryImpl(
                    tokenStorage = InMemoryTokenStorage(),
                    remoteDataSource =
                        DoacaoRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = cache,
                )

            val onlineRes = repo.listarHistoricoDoacoes().getOrThrow()
            assertFalse(onlineRes.desatualizado)
            assertEquals(2, onlineRes.dados.size)

            online = false
            val offlineRes = repo.listarHistoricoDoacoes().getOrThrow()
            assertTrue(offlineRes.desatualizado)
            assertEquals(2, offlineRes.dados.size)
        }

    @Test
    fun perfilRepository_carregaAtualizaESuportaFallbackOffline() =
        runTest {
            var online = true
            val mockEngine =
                MockEngine { request ->
                    if (!online) {
                        throw IllegalStateException("Backend offline")
                    }
                    when {
                        request.url.encodedPath.endsWith("/pessoa-fisica/1") ->
                            respond(
                                content =
                                    """
                                    {
                                      "id": 1,
                                      "nome": "Henrique Lima",
                                      "email": "henrique@solidar.io",
                                      "cpf": "12345678901",
                                      "logradouro": "Av. Salgado Filho",
                                      "numero": "3000",
                                      "bairro": "Lagoa Nova",
                                      "cep": "59078970"
                                    }
                                    """.trimIndent(),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        request.url.encodedPath.endsWith("/api/usuarios/perfil") ->
                            respond(
                                content = "",
                                status = HttpStatusCode.OK,
                            )
                        else ->
                            respond(
                                content = "",
                                status = HttpStatusCode.NotFound,
                            )
                    }
                }

            val tokenStorage =
                InMemoryTokenStorage().apply {
                    saveSession(
                        UserSession(
                            token = "jwt-token",
                            tipo = "PESSOA_FISICA",
                            id = 1,
                            nome = "Henrique",
                            email = "henrique@solidar.io",
                        ),
                    )
                }
            val cache = InMemoryLocalCacheStorage()
            val repo =
                PerfilRepositoryImpl(
                    tokenStorage = tokenStorage,
                    remoteDataSource =
                        PerfilRemoteDataSource(
                            httpClient = HttpClientFactory.create(engine = mockEngine),
                        ),
                    localCacheStorage = cache,
                )

            val onlinePerfil = repo.carregarPerfil().getOrThrow()
            assertFalse(onlinePerfil.desatualizado)
            assertEquals("Henrique Lima", onlinePerfil.dados.nome)
            assertEquals("123.456.789-01", onlinePerfil.dados.documentoFormatado)

            val atualizado =
                repo.atualizarPerfil(
                    AtualizarPerfilInput(
                        nome = "Henrique Barbosa Lima",
                        logradouro = "Rua Nova",
                        numero = "10",
                        bairro = "Tirol",
                        cep = "59020000",
                    ),
                ).getOrThrow()
            assertEquals("Henrique Barbosa Lima", atualizado.nome)
            assertEquals("Henrique Barbosa Lima", tokenStorage.getSession()?.nome)

            online = false
            val offlinePerfil = repo.carregarPerfil().getOrThrow()
            assertTrue(offlinePerfil.desatualizado)
            assertEquals("Henrique Barbosa Lima", offlinePerfil.dados.nome)
            assertEquals("Tirol", offlinePerfil.dados.bairro)
        }
}
