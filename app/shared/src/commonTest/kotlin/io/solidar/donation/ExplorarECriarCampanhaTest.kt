package io.solidar.donation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.solidar.donation.data.local.InMemoryLocalCacheStorage
import io.solidar.donation.data.remote.CampanhaRemoteDataSource
import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.repository.CampanhaRepositoryImpl
import io.solidar.donation.data.repository.DoacaoRepositoryImpl
import io.solidar.donation.data.storage.InMemoryTokenStorage
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.EfetivarDoacaoInput
import io.solidar.donation.domain.model.ItemCampanhaInput
import io.solidar.donation.domain.model.NovaCampanhaInput
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.model.Interessado
import io.solidar.donation.model.UserSession
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.campaign.CampanhaDetalheScreen
import io.solidar.donation.ui.campaign.CriarCampanhaContent
import io.solidar.donation.ui.campaign.CriarCampanhaState
import io.solidar.donation.ui.campaign.CriarCampanhaViewModel
import io.solidar.donation.ui.donation.DoarDinheiroContent
import io.solidar.donation.ui.donation.DoarDinheiroUiState
import io.solidar.donation.ui.donation.DoarItensCampanhaContent
import io.solidar.donation.ui.donation.DoarItensCampanhaUiState
import io.solidar.donation.ui.donation.DoarItensCampanhaViewModel
import io.solidar.donation.ui.donation.DoarItensEnvioState
import io.solidar.donation.ui.donation.EfetivarDoacaoContent
import io.solidar.donation.ui.donation.EfetivarDoacaoUiState
import io.solidar.donation.ui.donation.EfetivarDoacaoViewModel
import io.solidar.donation.ui.donation.EfetivarEnvioState
import io.solidar.donation.ui.explore.ExplorarScreen
import io.solidar.donation.ui.explore.ExplorarUiState
import io.solidar.donation.ui.explore.ExplorarViewModel
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.HistoricoViewModel
import io.solidar.donation.ui.home.HomeScreen
import io.solidar.donation.ui.home.InicioAba
import io.solidar.donation.ui.home.MainNavigationContent
import io.solidar.donation.ui.home.MainTab
import io.solidar.donation.ui.home.sampleCampaigns
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.util.CampanhaForm
import io.solidar.donation.util.ItemCampanhaForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeExplorarDoacaoRepository(
    var respostaDisponiveis: Result<ResultadoCacheado<List<DoacaoDisponivel>>> =
        Result.success(ResultadoCacheado(emptyList())),
    var respostaHistorico: Result<ResultadoCacheado<List<Doacao>>> =
        Result.success(ResultadoCacheado(emptyList())),
    var respostaInteresse: Result<Unit> = Result.success(Unit),
    var respostaInteressados: Result<List<Interessado>> = Result.success(emptyList()),
    var respostaEfetivar: Result<Doacao> =
        Result.success(
            Doacao(
                id = "42",
                itemTitle = "Casaco de frio infantil",
                counterpartName = "Carlos Lima",
                direction = DoacaoDirecao.FEITA,
                status = DoacaoStatus.CONCLUIDA,
                dateLabel = "Hoje",
            ),
        ),
    var atrasoMs: Long = 30L,
) : DoacaoRepository {
    private val interesses = mutableSetOf<String>()
    private val itensDoadosPorCampanha = mutableMapOf<String, MutableSet<String>>()
    private val quantidadesDoadasPorCampanha = mutableMapOf<String, MutableMap<String, Int>>()
    var ultimaCategoriaConsultada: String? = null
        private set
    var ultimoDonatarioId: Int? = null
        private set

    override suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray?,
        imagemNome: String?,
    ): Result<Doacao> = Result.failure(FalhaDominio.SemConexao())

    override suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaHistorico
    }

    override suspend fun listarDoacoesDisponiveis(
        categoria: String?,
        busca: String?,
    ): Result<ResultadoCacheado<List<DoacaoDisponivel>>> {
        ultimaCategoriaConsultada = categoria
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaDisponiveis
    }

    override suspend fun registrarInteresse(doacaoId: String): Result<Unit> {
        if (atrasoMs > 0) delay(atrasoMs)
        respostaInteresse.onSuccess { interesses.add(doacaoId) }
        return respostaInteresse
    }

    override suspend fun removerInteresse(doacaoId: String): Result<Unit> {
        if (atrasoMs > 0) delay(atrasoMs)
        respostaInteresse.onSuccess { interesses.remove(doacaoId) }
        return respostaInteresse
    }

    override fun listarInteressesIds(): Set<String> = interesses.toSet()

    override suspend fun listarInteressados(doacaoId: String): Result<List<Interessado>> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaInteressados
    }

    override suspend fun efetivarDoacao(
        doacaoId: String,
        input: EfetivarDoacaoInput,
    ): Result<Doacao> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaEfetivar
    }

    override suspend fun doarItensParaCampanha(
        campanhaId: String,
        campanhaTitulo: String,
        itens: List<NovaDoacaoInput>,
        itensIds: Set<String>,
        donatarioId: Int?,
        quantidadesPorItemId: Map<String, Int>,
    ): Result<Doacao> {
        if (atrasoMs > 0) delay(atrasoMs)
        ultimoDonatarioId = donatarioId
        itensDoadosPorCampanha.getOrPut(campanhaId) { mutableSetOf() }.addAll(itensIds)
        val mapaQtd = quantidadesDoadasPorCampanha.getOrPut(campanhaId) { mutableMapOf() }
        quantidadesPorItemId.forEach { (id, qtd) ->
            mapaQtd[id] = (mapaQtd[id] ?: 0) + qtd
        }
        return respostaEfetivar
    }

    override fun listarItensDoadosCampanha(campanhaId: String): Set<String> = itensDoadosPorCampanha[campanhaId]?.toSet().orEmpty()

    override fun listarQuantidadesDoadasCampanha(campanhaId: String): Map<String, Int> =
        quantidadesDoadasPorCampanha[campanhaId]?.toMap().orEmpty()
}

private class FakeCriarCampanhaRepository(
    var respostaCriar: Result<Campaign>,
    var atrasoMs: Long = 30L,
) : CampanhaRepository {
    override suspend fun listarCampanhas(): Result<ResultadoCacheado<List<Campaign>>> = Result.success(ResultadoCacheado(emptyList()))

    override suspend fun obterCampanhaPorId(id: String): Result<Campaign?> = Result.success(null)

    override suspend fun criarCampanha(input: NovaCampanhaInput): Result<Campaign> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaCriar
    }

    override fun listarFavoritosIds(): Set<String> = emptySet()

    override fun alternarFavorito(campanhaId: String): Set<String> = emptySet()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
class ExplorarECriarCampanhaTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private val doacaoExemplo =
        DoacaoDisponivel(
            id = "42",
            itemTitle = "Casaco de frio infantil",
            quantidade = 2,
            categoria = "Roupas",
            condicao = "Usado",
            descricao = "Tamanho 10 em ótimo estado.",
            motivo = "Criança cresceu",
            nomeDoador = "Ana Souza",
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doacaoRepository_listarDisponiveis_salvaCacheEDevolveOfflineComoDesatualizado() =
        runTest {
            var online = true
            val engine =
                MockEngine {
                    if (online) {
                        respond(
                            content =
                                """
                                {
                                  "content": [
                                    {
                                      "id": 42,
                                      "nomeDoador": "Ana Souza",
                                      "itens": [
                                        {
                                          "nome": "Casaco de frio infantil",
                                          "quantidade": 2,
                                          "categoria": "ROUPAS",
                                          "eNovo": "N",
                                          "descricao": "Tamanho 10"
                                        }
                                      ]
                                    }
                                  ],
                                  "totalElements": 1,
                                  "totalPages": 1,
                                  "currentPage": 0
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    } else {
                        throw IOException("Sem rede")
                    }
                }
            val cache = InMemoryLocalCacheStorage()
            val repo =
                DoacaoRepositoryImpl(
                    tokenStorage = InMemoryTokenStorage(),
                    remoteDataSource = DoacaoRemoteDataSource(httpClient = HttpClientFactory.create(engine = engine)),
                    localCacheStorage = cache,
                )

            val onlineRes = repo.listarDoacoesDisponiveis().getOrThrow()
            assertFalse(onlineRes.desatualizado)
            assertEquals(1, onlineRes.dados.size)
            assertEquals("Casaco de frio infantil", onlineRes.dados.first().itemTitle)

            online = false
            val offlineRes = repo.listarDoacoesDisponiveis().getOrThrow()
            assertTrue(offlineRes.desatualizado)
            assertEquals(1, offlineRes.dados.size)
        }

    @Test
    fun campanhaRepository_criarCampanha_enviaParaBackendEAtualizaCacheLocal() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                              "id": 77,
                              "nomeInstituicao": "ONG Semear",
                              "titulo": "Agasalho Solidário",
                              "descricao": "Campanha de inverno",
                              "itensFoco": "Casacos (15 un.), Cobertores (10 un.)",
                              "metaVoluntarios": 25
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.Created,
                        headers = jsonHeaders,
                    )
                }
            val cache = InMemoryLocalCacheStorage()
            val repo =
                CampanhaRepositoryImpl(
                    remoteDataSource = CampanhaRemoteDataSource(httpClient = HttpClientFactory.create(engine = engine)),
                    localCacheStorage = cache,
                )

            val criada =
                repo.criarCampanha(
                    NovaCampanhaInput(
                        titulo = "Agasalho Solidário",
                        descricao = "Campanha de inverno",
                        itens =
                            listOf(
                                ItemCampanhaInput("Casacos", 15),
                                ItemCampanhaInput("Cobertores", 10),
                            ),
                    ),
                ).getOrThrow()

            assertEquals("77", criada.id)
            assertEquals("Agasalho Solidário", criada.title)
            assertEquals(2, criada.itensNecessarios.size)
            assertEquals("Casacos", criada.itensNecessarios[0].nome)
            assertEquals("15 unidades", criada.itensNecessarios[0].quantidadeDescricao)
            assertEquals("Cobertores", criada.itensNecessarios[1].nome)
            assertEquals("10 unidades", criada.itensNecessarios[1].quantidadeDescricao)
            assertEquals(1, cache.lerCampanhas()?.size)
        }

    @Test
    fun explorarViewModel_carregaDoacoesEAlternaInteresse() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val repo =
                FakeExplorarDoacaoRepository(
                    respostaDisponiveis = Result.success(ResultadoCacheado(listOf(doacaoExemplo))),
                )
            val vm = ExplorarViewModel(repo)

            vm.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val sucesso = assertIs<TelaEstado.Sucesso<List<DoacaoDisponivel>>>(awaitItem().estadoLista)
                assertEquals(1, sucesso.dados.size)

                vm.alternarInteresse("42")
                val processando = awaitItem()
                assertEquals("42", processando.processandoInteresseId)
                val aposInteresse = awaitItem()
                assertTrue("42" in aposInteresse.interessesIds)
                assertEquals("Interesse registrado! O doador foi notificado.", aposInteresse.avisoInteresse)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun criarCampanhaViewModel_falhaMantemDadosDoFormularioEMostraErro() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val repo =
                FakeCriarCampanhaRepository(
                    respostaCriar = Result.failure(FalhaDominio.DadosInvalidos("Apenas instituições podem criar campanhas.")),
                )
            val vm = CriarCampanhaViewModel(repo)
            val formPreenchido =
                CampanhaForm(
                    titulo = "Campanha Solidária",
                    descricao = "Apoio comunitário",
                    itens =
                        listOf(
                            ItemCampanhaForm(id = 1, nome = "Alimentos", quantidade = 10),
                            ItemCampanhaForm(id = 2, nome = "Higiene", quantidade = 5),
                        ),
                )
            vm.onFormChange(formPreenchido)

            vm.uiState.test {
                assertIs<CriarCampanhaState.Idle>(awaitItem().envioState)

                vm.criarCampanha()
                assertIs<CriarCampanhaState.Loading>(awaitItem().envioState)
                val aposErro = awaitItem()
                val erro = assertIs<CriarCampanhaState.Error>(aposErro.envioState)
                assertEquals("Apenas instituições podem criar campanhas.", erro.message)
                assertEquals(formPreenchido, aposErro.form)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun explorarScreen_exibeDoacaoERegistraInteresseNoClique() =
        runComposeUiTest {
            var interessado by mutableStateOf(false)

            setContent {
                DonationTheme {
                    ExplorarScreen(
                        uiState =
                            ExplorarUiState(
                                estadoLista = TelaEstado.Sucesso(listOf(doacaoExemplo)),
                                interessesIds = if (interessado) setOf("42") else emptySet(),
                            ),
                        onBuscaChange = {},
                        onBuscar = {},
                        onCategoriaSelecionada = {},
                        onAlternarInteresse = { interessado = !interessado },
                        onTentarDeNovo = {},
                        onLimparAviso = {},
                    )
                }
            }

            onNodeWithText("Explorar doações").assertIsDisplayed()
            onNodeWithText("Casaco de frio infantil").assertIsDisplayed()
            onNodeWithText("Tenho interesse").performClick()
            waitForIdle()

            assertTrue(interessado)
            onNodeWithText("Interesse registrado").assertIsDisplayed()
        }

    @Test
    fun criarCampanhaScreen_validaFormularioInvalidoEValido() =
        runComposeUiTest {
            var form by mutableStateOf(CampanhaForm(titulo = "Oi"))
            var submetido = false

            setContent {
                DonationTheme {
                    CriarCampanhaContent(
                        state = CriarCampanhaState.Idle,
                        form = form,
                        onFormChange = { form = it },
                        onNavigateBack = {},
                        onSubmit = { submetido = true },
                    )
                }
            }

            onNodeWithText("Use pelo menos 3 caracteres.").assertIsDisplayed()
            onNodeWithText("Publicar Campanha").performScrollTo().assertIsNotEnabled()

            // Adiciona um segundo item pela UI e define quantidades individuais
            onNodeWithText("Adicionar outro item").performScrollTo().performClick()
            waitForIdle()
            assertEquals(2, form.itens.size)

            form =
                form.copy(
                    titulo = "Cestas básicas para o bairro das Rocas",
                    itens =
                        listOf(
                            ItemCampanhaForm(id = 1, nome = "Arroz", quantidade = 10),
                            ItemCampanhaForm(id = 2, nome = "Feijão", quantidade = 5),
                        ),
                )
            waitForIdle()

            onNodeWithText("Total: 15 un.").performScrollTo().assertIsDisplayed()
            onNodeWithText("Publicar Campanha").performScrollTo().assertIsEnabled().performClick()
            assertTrue(submetido)
        }

    @Test
    fun doacaoRepository_listarInteressadosEEfetivar_atualizaStatusParaConcluida() =
        runTest {
            val engine =
                MockEngine { request ->
                    when {
                        request.url.encodedPath.endsWith("/interessados") ->
                            respond(
                                content =
                                    """
                                    [
                                      {"id": 9, "nome": "Carlos Lima", "email": "carlos@solidar.io", "bairro": "Rocas"}
                                    ]
                                    """.trimIndent(),
                                status = HttpStatusCode.OK,
                                headers = jsonHeaders,
                            )
                        request.url.encodedPath.endsWith("/efetivar") ->
                            respond(
                                content =
                                    """
                                    {
                                      "id": 42,
                                      "donatarioId": 9,
                                      "nomeDonatario": "Carlos Lima",
                                      "itens": [{"nome": "Casaco de frio infantil", "quantidade": 2}]
                                    }
                                    """.trimIndent(),
                                status = HttpStatusCode.OK,
                                headers = jsonHeaders,
                            )
                        else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                    }
                }
            val cache = InMemoryLocalCacheStorage()
            cache.salvarDoacoesDisponiveis(listOf(doacaoExemplo))
            val repo =
                DoacaoRepositoryImpl(
                    tokenStorage = InMemoryTokenStorage(),
                    remoteDataSource = DoacaoRemoteDataSource(httpClient = HttpClientFactory.create(engine = engine)),
                    localCacheStorage = cache,
                )

            val interessados = repo.listarInteressados("42").getOrThrow()
            assertEquals(1, interessados.size)
            assertEquals("Carlos Lima", interessados.first().nome)

            val efetivada =
                repo.efetivarDoacao(
                    "42",
                    EfetivarDoacaoInput(donatarioId = 9, nomeLocal = "Portaria do IMD"),
                ).getOrThrow()
            assertEquals(DoacaoStatus.CONCLUIDA, efetivada.status)
            assertEquals("Carlos Lima", efetivada.counterpartName)
            assertTrue(cache.lerDoacoesDisponiveis().orEmpty().isEmpty())
        }

    @Test
    fun efetivarDoacaoViewModel_carregaInteressadosEConfirmaEntrega() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val interessado = Interessado(id = 9, nome = "Carlos Lima", email = "carlos@solidar.io", enderecoResumo = "Rocas")
            val repo = FakeExplorarDoacaoRepository(respostaInteressados = Result.success(listOf(interessado)))
            val vm = EfetivarDoacaoViewModel(repo)

            vm.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoInteressados)

                vm.carregarInteressados("42")
                val carregado = awaitItem()
                val sucesso = assertIs<TelaEstado.Sucesso<List<Interessado>>>(carregado.estadoInteressados)
                assertEquals(1, sucesso.dados.size)
                assertEquals(9, carregado.interessadoSelecionadoId)

                vm.onNomeLocalChange("Portaria do IMD")
                assertTrue(awaitItem().podeConfirmar)

                vm.confirmarEfetivacao("42")
                assertIs<EfetivarEnvioState.Loading>(awaitItem().envioState)
                assertIs<EfetivarEnvioState.Success>(awaitItem().envioState)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun efetivarDoacaoScreen_selecionaInteressadoEConfirmaEntrega() =
        runComposeUiTest {
            var nomeLocal by mutableStateOf("")
            var confirmou = false

            setContent {
                DonationTheme {
                    EfetivarDoacaoContent(
                        itemTitle = "Casaco de frio infantil",
                        uiState =
                            EfetivarDoacaoUiState(
                                estadoInteressados =
                                    TelaEstado.Sucesso(
                                        listOf(Interessado(9, "Carlos Lima", "carlos@solidar.io", "Rocas")),
                                    ),
                                interessadoSelecionadoId = 9,
                                nomeLocal = nomeLocal,
                            ),
                        onNavigateBack = {},
                        onRecarregar = {},
                        onSelecionarInteressado = {},
                        onNomeLocalChange = { nomeLocal = it },
                        onLogradouroChange = {},
                        onBairroChange = {},
                        onConfirmar = { confirmou = true },
                    )
                }
            }

            onNodeWithText("Carlos Lima").assertIsDisplayed()
            onNodeWithText("Confirmar entrega").performScrollTo().assertIsNotEnabled()

            nomeLocal = "Portaria do IMD"
            waitForIdle()

            onNodeWithText("Confirmar entrega").performScrollTo().assertIsEnabled().performClick()
            assertTrue(confirmou)
        }

    @Test
    fun doarItensCampanhaViewModel_removeItensJaDoadosAposConfirmacao() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val campanha =
                Campaign(
                    id = "1",
                    title = "Cestas básicas",
                    organizer = "ONG Paz",
                    goalInCents = 500_000L,
                    raisedInCents = 100_000L,
                    instituicaoId = 77,
                    itensNecessarios =
                        listOf(
                            CampaignItem("item_1", "Arroz", "2 kg", "food"),
                            CampaignItem("item_2", "Feijão", "2 kg", "food"),
                        ),
                )
            val repo = FakeExplorarDoacaoRepository()
            val vm = DoarItensCampanhaViewModel(repo)

            vm.carregarItensPendentes(campanha)
            assertEquals(2, vm.uiState.value.itensPendentes.size)

            vm.alternarSelecaoItem("item_1")
            vm.alterarQuantidade("item_1", 1)

            vm.uiState.test {
                assertEquals(2, awaitItem().quantidadesSelecionadas["item_1"])

                vm.confirmarDoacao(campanha)
                assertIs<DoarItensEnvioState.Loading>(awaitItem().envioState)
                val aposSucesso = awaitItem()
                assertIs<DoarItensEnvioState.Success>(aposSucesso.envioState)
                // Donatário (instituição dona da campanha) vinculado para constar em Recebidas
                assertEquals(77, repo.ultimoDonatarioId)
                // Apenas o item ainda não doado ("Feijão") permanece na lista
                assertEquals(listOf("item_2"), aposSucesso.itensPendentes.map { it.id })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun doarItensEDoarDinheiroScreens_interacoesDeSelecaoEValorPix() =
        runComposeUiTest {
            val campanha =
                Campaign(
                    id = "1",
                    title = "Cestas básicas",
                    organizer = "ONG Paz",
                    goalInCents = 500_000L,
                    raisedInCents = 100_000L,
                    itensNecessarios = listOf(CampaignItem("item_1", "Alimentos não perecíveis", "2 un.", "food")),
                )
            var selecionados by mutableStateOf<Map<String, Int>>(emptyMap())
            var confirmouItens = false

            setContent {
                DonationTheme {
                    DoarItensCampanhaContent(
                        campaign = campanha,
                        uiState =
                            DoarItensCampanhaUiState(
                                itensPendentes = campanha.itensNecessarios,
                                quantidadesSelecionadas = selecionados,
                            ),
                        onNavigateBack = {},
                        onAlternarItem = { id ->
                            selecionados =
                                if (id in selecionados) selecionados - id else selecionados + (id to 1)
                        },
                        onAlterarQuantidade = { _, _ -> },
                        onObservacaoChange = {},
                        onConfirmar = { confirmouItens = true },
                    )
                }
            }

            onNodeWithText("Confirmar doação").performScrollTo().assertIsNotEnabled()
            onNodeWithText("Alimentos não perecíveis").performClick()
            waitForIdle()
            onNodeWithText("Confirmar doação (1)").performScrollTo().assertIsEnabled().performClick()
            assertTrue(confirmouItens)
        }

    @Test
    fun doarDinheiroScreen_copiaChaveEConfirmaContribuicao() =
        runComposeUiTest {
            val campanha =
                Campaign(
                    id = "1",
                    title = "Cestas básicas",
                    organizer = "ONG Paz",
                    goalInCents = 500_000L,
                    raisedInCents = 100_000L,
                )
            var copiou = false
            var confirmou = false

            setContent {
                DonationTheme {
                    DoarDinheiroContent(
                        campaign = campanha,
                        chavePix = "ongpaz@solidar.io",
                        uiState = DoarDinheiroUiState(valorReaisTexto = "100", chaveCopiada = copiou),
                        onNavigateBack = {},
                        onValorChange = {},
                        onSelecionarValor = {},
                        onCopiarChave = { copiou = true },
                        onConfirmar = { confirmou = true },
                    )
                }
            }

            onNodeWithText("Copiar chave PIX").performScrollTo().performClick()
            waitForIdle()
            assertTrue(copiou)
            onNodeWithText("Confirmar contribuição (R$ 100,00)").performScrollTo().assertIsEnabled().performClick()
            assertTrue(confirmou)
        }

    @Test
    fun homeScreen_alternaEntreAbasPedemEOferecemDoacoes() =
        runComposeUiTest {
            var abaAtual by mutableStateOf(InicioAba.PEDIDOS)
            var interessado by mutableStateOf(false)

            setContent {
                DonationTheme {
                    HomeScreen(
                        estado = TelaEstado.Sucesso(sampleCampaigns),
                        onCampaignClick = {},
                        onTentarDeNovo = {},
                        exibirAbasTipo = true,
                        abaSelecionada = abaAtual,
                        onAbaSelecionada = { abaAtual = it },
                        estadoDoacoesDisponiveis = TelaEstado.Sucesso(listOf(doacaoExemplo)),
                        interessesIds = if (interessado) setOf("42") else emptySet(),
                        onAlternarInteresse = { interessado = !interessado },
                    )
                }
            }

            onNodeWithText("Pedem doações").assertIsDisplayed()
            onNodeWithText("Oferecem doações").assertIsDisplayed()
            onNodeWithText(sampleCampaigns.first().title).assertIsDisplayed()

            onNodeWithText("Oferecem doações").performClick()
            waitForIdle()

            assertEquals(InicioAba.OFERTAS, abaAtual)
            onNodeWithText("Casaco de frio infantil").assertIsDisplayed()
            onNodeWithText("Tenho interesse").performClick()
            waitForIdle()
            assertTrue(interessado)
        }

    @Test
    fun historicoScreen_exibeAbaCampanhasCriadasParaInstituicao() =
        runComposeUiTest {
            var abaAtual by mutableStateOf(HistoricoAba.FEITAS)
            var campanhaClicada by mutableStateOf<String?>(null)

            setContent {
                DonationTheme {
                    HistoricoScreen(
                        estado = TelaEstado.Vazio,
                        selectedTab = abaAtual,
                        onTabSelected = { abaAtual = it },
                        onTentarDeNovo = {},
                        exibirAbaCampanhas = true,
                        estadoMinhasCampanhas = TelaEstado.Sucesso(sampleCampaigns.take(1)),
                        onCampaignClick = { campanhaClicada = it.id },
                    )
                }
            }

            onNodeWithText("Campanhas").assertIsDisplayed().performClick()
            waitForIdle()

            assertEquals(HistoricoAba.CAMPANHAS, abaAtual)
            onNodeWithText(sampleCampaigns.first().title).assertIsDisplayed().performClick()
            assertEquals(sampleCampaigns.first().id, campanhaClicada)
        }

    @Test
    fun mainNavigation_botaoMais_ofereceEscolhaParaInstituicaoEVaiDiretoParaPf() =
        runComposeUiTest {
            var abriuArrecadacao = false
            var abriuDoacao = false
            var contaInstituicao by mutableStateOf(true)

            setContent {
                DonationTheme {
                    MainNavigationContent(
                        wide = true,
                        selectedTab = MainTab.HOME,
                        onTabSelected = {},
                        historicoTab = HistoricoAba.FEITAS,
                        onHistoricoTabSelected = {},
                        onCampaignClick = {},
                        onNavigateToCreateDonation = { abriuDoacao = true },
                        onNavigateToCreateCampaign =
                            if (contaInstituicao) {
                                { abriuArrecadacao = true }
                            } else {
                                null
                            },
                        onLogout = {},
                    )
                }
            }

            // Instituição (PJ/ONG): botão + abre o diálogo com as duas opções
            onNodeWithContentDescription("Criar campanha ou doação").performClick()
            waitForIdle()
            onNodeWithText("O que deseja cadastrar?").assertIsDisplayed()
            onNodeWithText("Campanha de arrecadação").assertIsDisplayed().performClick()
            waitForIdle()
            assertTrue(abriuArrecadacao)

            // Reabre e escolhe Campanha de doação
            onNodeWithContentDescription("Criar campanha ou doação").performClick()
            waitForIdle()
            onNodeWithText("Campanha de doação").assertIsDisplayed().performClick()
            waitForIdle()
            assertTrue(abriuDoacao)

            // Pessoa Física: botão + vai direto para cadastrar doação sem abrir diálogo
            abriuDoacao = false
            contaInstituicao = false
            waitForIdle()
            onNodeWithContentDescription("Criar doação").performClick()
            waitForIdle()
            assertTrue(abriuDoacao)
        }

    @Test
    fun campanhaDetalheScreen_bloqueiaDoacaoParaOrganizadorDaCampanha() =
        runComposeUiTest {
            setContent {
                DonationTheme {
                    CampanhaDetalheScreen(
                        campaign = sampleCampaigns.first(),
                        onBack = {},
                        onQueroDoar = {},
                        ehOrganizador = true,
                    )
                }
            }

            onNodeWithText("Você é o organizador desta campanha").assertIsDisplayed()
            onNodeWithText("Quero doar").assertDoesNotExist()
        }

    @Test
    fun historicoViewModel_separaInteressadosDeFeitasEMantemRecebidas() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val doacaoCampanha =
                Doacao("1", "Cesta básica", "ONG Luz", DoacaoDirecao.FEITA, DoacaoStatus.CONCLUIDA, "Hoje")
            val ofertaPendente =
                Doacao("2", "Casaco infantil", "Aguardando interessado", DoacaoDirecao.FEITA, DoacaoStatus.PENDENTE, "Hoje")
            val doacaoRecebida =
                Doacao("3", "Livros", "Maria", DoacaoDirecao.RECEBIDA, DoacaoStatus.CONCLUIDA, "Hoje")
            val repo =
                FakeExplorarDoacaoRepository(
                    respostaHistorico =
                        Result.success(ResultadoCacheado(listOf(doacaoCampanha, ofertaPendente, doacaoRecebida))),
                )
            val vm = HistoricoViewModel(repo)

            vm.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val feitas = assertIs<TelaEstado.Sucesso<List<Doacao>>>(awaitItem().estadoLista)
                assertEquals(listOf("1"), feitas.dados.map { it.id })

                vm.selecionarAba(HistoricoAba.INTERESSADOS)
                val listaInteressados = assertIs<TelaEstado.Sucesso<List<Doacao>>>(awaitItem().estadoLista)
                assertEquals(listOf("2"), listaInteressados.dados.map { it.id })

                vm.selecionarAba(HistoricoAba.RECEBIDAS)
                val listaRecebidas = assertIs<TelaEstado.Sucesso<List<Doacao>>>(awaitItem().estadoLista)
                assertEquals(listOf("3"), listaRecebidas.dados.map { it.id })

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun efetivarDoacaoScreen_exibeMensagemDoInteressadoEDialogoEnviaMensagem() =
        runComposeUiTest {
            var mensagemEnviada by mutableStateOf<String?>(null)

            setContent {
                DonationTheme {
                    ExplorarScreen(
                        uiState = ExplorarUiState(estadoLista = TelaEstado.Sucesso(listOf(doacaoExemplo))),
                        onBuscaChange = {},
                        onBuscar = {},
                        onCategoriaSelecionada = {},
                        onAlternarInteresse = {},
                        onRegistrarInteresseComMensagem = { _, msg -> mensagemEnviada = msg },
                        onTentarDeNovo = {},
                        onLimparAviso = {},
                    )
                }
            }

            onNodeWithText("Tenho interesse").performClick()
            waitForIdle()
            onNodeWithText("Demonstrar interesse").assertIsDisplayed()
            onNodeWithText("Mensagem para o doador").performTextInput("Posso retirar amanhã!")
            onNodeWithText("Enviar interesse").performClick()
            waitForIdle()

            assertEquals("Posso retirar amanhã!", mensagemEnviada)
        }

    @Test
    fun efetivarDoacaoScreen_mostraListaDeInteressadosComMensagem() =
        runComposeUiTest {
            setContent {
                DonationTheme {
                    EfetivarDoacaoContent(
                        itemTitle = "Casaco de frio infantil",
                        uiState =
                            EfetivarDoacaoUiState(
                                estadoInteressados =
                                    TelaEstado.Sucesso(
                                        listOf(
                                            Interessado(
                                                id = 9,
                                                nome = "Carlos Lima",
                                                email = "carlos@solidar.io",
                                                enderecoResumo = "Rocas",
                                                mensagem = "Preciso para meu filho, posso buscar hoje.",
                                            ),
                                        ),
                                    ),
                                interessadoSelecionadoId = 9,
                            ),
                        onNavigateBack = {},
                        onRecarregar = {},
                        onSelecionarInteressado = {},
                        onNomeLocalChange = {},
                        onLogradouroChange = {},
                        onBairroChange = {},
                        onConfirmar = {},
                    )
                }
            }

            onNodeWithText("Interessados nesta doação (1) *").assertIsDisplayed()
            onNodeWithText("Carlos Lima").assertIsDisplayed()
            onNodeWithText("“Preciso para meu filho, posso buscar hoje.”").assertIsDisplayed()
        }

    @Test
    fun campanhaRepository_favoritosSaoIsoladosPorConta() {
        val tokenStorage = InMemoryTokenStorage()
        val cache = InMemoryLocalCacheStorage()
        val repo =
            CampanhaRepositoryImpl(
                localCacheStorage = cache,
                tokenStorage = tokenStorage,
            )

        tokenStorage.saveSession(UserSession("tok1", "PESSOA_FISICA", 1, "Conta 1", "c1@solidar.io"))
        repo.alternarFavorito("10")
        assertEquals(setOf("10"), repo.listarFavoritosIds())

        // Ao trocar para outra conta, não herda os favoritos da conta anterior
        tokenStorage.saveSession(UserSession("tok2", "INSTITUICAO", 2, "Conta 2", "c2@solidar.io"))
        assertEquals(emptySet(), repo.listarFavoritosIds())
        repo.alternarFavorito("20")
        assertEquals(setOf("20"), repo.listarFavoritosIds())

        // Ao voltar para a Conta 1, seus favoritos continuam preservados
        tokenStorage.saveSession(UserSession("tok1", "PESSOA_FISICA", 1, "Conta 1", "c1@solidar.io"))
        assertEquals(setOf("10"), repo.listarFavoritosIds())
    }

    @Test
    fun doarItensCampanhaViewModel_mantemUnidadesRestantesQuandoDoacaoEhParcial() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val campanha =
                Campaign(
                    id = "8",
                    title = "Casacos de inverno",
                    organizer = "ONG Calor",
                    goalInCents = 100_000L,
                    raisedInCents = 0L,
                    instituicaoId = 2,
                    itensNecessarios =
                        listOf(
                            CampaignItem("8_item_0", "Casacos", "10 unidades", "check", quantidadeAlvo = 10),
                        ),
                )
            val repo = FakeExplorarDoacaoRepository()
            val vm = DoarItensCampanhaViewModel(repo)

            vm.carregarItensPendentes(campanha)
            vm.alternarSelecaoItem("8_item_0")
            vm.alterarQuantidade("8_item_0", 2) // 3 unidades

            vm.uiState.test {
                assertEquals(3, awaitItem().quantidadesSelecionadas["8_item_0"])
                vm.confirmarDoacao(campanha)
                assertIs<DoarItensEnvioState.Loading>(awaitItem().envioState)
                val aposSucesso = awaitItem()
                assertIs<DoarItensEnvioState.Success>(aposSucesso.envioState)
                // Ainda restam 7 de 10 unidades, logo o item permanece disponível
                assertEquals(1, aposSucesso.itensPendentes.size)
                assertEquals("Faltam 7 de 10 un.", aposSucesso.itensPendentes.first().quantidadeDescricao)
                assertEquals(7, aposSucesso.itensPendentes.first().quantidadeAlvo)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun campanhaDetalheScreen_organizadorCancelaCampanhaComConfirmacao() =
        runComposeUiTest {
            var cancelou = false

            setContent {
                DonationTheme {
                    CampanhaDetalheScreen(
                        campaign = sampleCampaigns.first(),
                        onBack = {},
                        onQueroDoar = {},
                        ehOrganizador = true,
                        onCancelarCampanha = { cancelou = true },
                    )
                }
            }

            onNodeWithText("Cancelar campanha").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Cancelar campanha?").assertIsDisplayed()
            onNodeWithText("Confirmar cancelamento").performClick()
            waitForIdle()
            assertTrue(cancelou)
        }

    @Test
    fun historicoScreen_abaInteressadosPermiteCancelarDoacaoPendente() =
        runComposeUiTest {
            val oferta = Doacao("5", "Roupas (11 un.)", "Aguardando interessado", DoacaoDirecao.FEITA, DoacaoStatus.PENDENTE, "Hoje")
            var idCancelado by mutableStateOf<String?>(null)

            setContent {
                DonationTheme {
                    HistoricoScreen(
                        estado = TelaEstado.Sucesso(listOf(oferta)),
                        selectedTab = HistoricoAba.INTERESSADOS,
                        onTabSelected = {},
                        onTentarDeNovo = {},
                        exibirAbaInteressados = true,
                        onEfetivarDoacao = {},
                        onCancelarDoacao = { idCancelado = it.id },
                    )
                }
            }

            onNodeWithText("Cancelar doação").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Cancelar doação?").assertIsDisplayed()
            onNodeWithText("Confirmar cancelamento").performClick()
            waitForIdle()
            assertEquals("5", idCancelado)
        }

    @Test
    fun explorarScreen_ocultaBotaoInteresseParaCriadorDaPropriaOferta() =
        runComposeUiTest {
            val minhaOferta = doacaoExemplo.copy(doadorId = 10)

            setContent {
                DonationTheme {
                    ExplorarScreen(
                        uiState = ExplorarUiState(estadoLista = TelaEstado.Sucesso(listOf(minhaOferta))),
                        onBuscaChange = {},
                        onBuscar = {},
                        onCategoriaSelecionada = {},
                        onAlternarInteresse = {},
                        onTentarDeNovo = {},
                        onLimparAviso = {},
                        usuarioLogadoId = 10,
                    )
                }
            }

            onNodeWithText("Sua oferta de doação").assertIsDisplayed()
            onNodeWithText("Tenho interesse").assertDoesNotExist()
        }

    @Test
    fun historicoScreen_abaCampanhasExibeDetalhesProgressoEFinalizarOuCancelar() =
        runComposeUiTest {
            var finalizouId by mutableStateOf<String?>(null)
            var cancelouId by mutableStateOf<String?>(null)
            val campanha = sampleCampaigns.first()

            setContent {
                DonationTheme {
                    HistoricoScreen(
                        estado = TelaEstado.Vazio,
                        selectedTab = HistoricoAba.CAMPANHAS,
                        onTabSelected = {},
                        onTentarDeNovo = {},
                        exibirAbaCampanhas = true,
                        estadoMinhasCampanhas = TelaEstado.Sucesso(listOf(campanha)),
                        onFinalizarCampanha = { finalizouId = it.id },
                        onCancelarCampanha = { cancelouId = it.id },
                    )
                }
            }

            onNodeWithText(campanha.title).assertIsDisplayed()
            onNodeWithText("Ativa").assertIsDisplayed()
            onNodeWithText("Finalizar").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Confirmar finalização").performClick()
            waitForIdle()
            assertEquals(campanha.id, finalizouId)

            onNodeWithText("Cancelar").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Confirmar cancelamento").performClick()
            waitForIdle()
            assertEquals(campanha.id, cancelouId)
        }
}
