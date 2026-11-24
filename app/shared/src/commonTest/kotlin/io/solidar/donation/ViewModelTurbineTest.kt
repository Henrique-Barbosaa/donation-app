package io.solidar.donation

import app.cash.turbine.test
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.ui.donation.CriarDoacaoState
import io.solidar.donation.ui.donation.CriarDoacaoViewModel
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoViewModel
import io.solidar.donation.ui.home.CampanhasViewModel
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.util.DoacaoForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Dublê configurável de [CampanhaRepository] com atraso em tempo virtual para testar transições no Turbine.
 */
private class FakeCampanhaRepository(
    var resposta: Result<ResultadoCacheado<List<Campaign>>> =
        Result.success(ResultadoCacheado(emptyList())),
    var atrasoMs: Long = 50L,
) : CampanhaRepository {
    private val favoritos = mutableSetOf<String>()

    override suspend fun listarCampanhas(): Result<ResultadoCacheado<List<Campaign>>> {
        if (atrasoMs > 0) delay(atrasoMs)
        return resposta
    }

    override suspend fun obterCampanhaPorId(id: String): Result<Campaign?> {
        if (atrasoMs > 0) delay(atrasoMs)
        return resposta.map { res -> res.dados.firstOrNull { it.id == id } }
    }

    override fun listarFavoritosIds(): Set<String> = favoritos.toSet()

    override fun alternarFavorito(campanhaId: String): Set<String> {
        if (!favoritos.add(campanhaId)) {
            favoritos.remove(campanhaId)
        }
        return favoritos.toSet()
    }
}

/**
 * Dublê configurável de [DoacaoRepository] com atraso em tempo virtual.
 */
private class FakeDoacaoRepository(
    var respostaHistorico: Result<ResultadoCacheado<List<Doacao>>> =
        Result.success(ResultadoCacheado(emptyList())),
    var respostaCriacao: Result<Doacao> =
        Result.failure(FalhaDominio.SemConexao()),
    var atrasoMs: Long = 50L,
) : DoacaoRepository {
    override suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray?,
        imagemNome: String?,
    ): Result<Doacao> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaCriacao
    }

    override suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>> {
        if (atrasoMs > 0) delay(atrasoMs)
        return respostaHistorico
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTurbineTest {
    @BeforeTest
    fun setUp() {
        // O dispatcher será configurado em cada runTest com StandardTestDispatcher(testScheduler)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun campanhasViewModel_transicionaDeCarregandoParaSucesso() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val campanhaExemplo =
                Campaign(
                    id = "1",
                    title = "Campanha Solidária",
                    organizer = "ONG Teste",
                    goalInCents = 100_000L,
                    raisedInCents = 50_000L,
                )
            val fakeRepo =
                FakeCampanhaRepository(
                    resposta = Result.success(ResultadoCacheado(listOf(campanhaExemplo))),
                )

            val viewModel = CampanhasViewModel(fakeRepo)

            viewModel.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val sucesso = assertIs<TelaEstado.Sucesso<List<Campaign>>>(awaitItem().estadoLista)
                assertEquals(1, sucesso.dados.size)
                assertEquals("Campanha Solidária", sucesso.dados.first().title)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun campanhasViewModel_transicionaDeCarregandoParaVazio() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val fakeRepo =
                FakeCampanhaRepository(
                    resposta = Result.success(ResultadoCacheado(emptyList())),
                )

            val viewModel = CampanhasViewModel(fakeRepo)

            viewModel.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                assertIs<TelaEstado.Vazio>(awaitItem().estadoLista)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun campanhasViewModel_falhasSemConexaoERespostaInvalida_geramMensagensDistintas() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val fakeRepo =
                FakeCampanhaRepository(
                    resposta = Result.failure(FalhaDominio.SemConexao()),
                )

            val viewModel = CampanhasViewModel(fakeRepo)

            viewModel.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val erroSemConexao = assertIs<TelaEstado.Erro>(awaitItem().estadoLista)
                assertTrue(erroSemConexao.mensagem.contains("Sem conexão", ignoreCase = true))

                // Agora simula RespostaInvalida (HTTP 500) e tenta de novo
                fakeRepo.resposta = Result.failure(FalhaDominio.RespostaInvalida(statusCode = 500))
                viewModel.carregarCampanhas()

                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val erroRespostaInvalida = assertIs<TelaEstado.Erro>(awaitItem().estadoLista)
                assertTrue(erroRespostaInvalida.mensagem.contains("resposta inválida", ignoreCase = true))
                assertNotEquals(erroSemConexao.mensagem, erroRespostaInvalida.mensagem)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun campanhasViewModel_tentarDeNovoAposErro_recuperaEstadoParaSucesso() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val fakeRepo =
                FakeCampanhaRepository(
                    resposta = Result.failure(FalhaDominio.SemConexao()),
                )

            val viewModel = CampanhasViewModel(fakeRepo)

            viewModel.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                assertIs<TelaEstado.Erro>(awaitItem().estadoLista)

                // Configura resposta de sucesso e aciona o "Tentar de novo"
                fakeRepo.resposta =
                    Result.success(
                        ResultadoCacheado(
                            listOf(
                                Campaign(
                                    id = "99",
                                    title = "Campanha Recuperada",
                                    organizer = "Instituto Vida",
                                    goalInCents = 200_000L,
                                    raisedInCents = 100_000L,
                                ),
                            ),
                        ),
                    )
                viewModel.carregarCampanhas()

                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)
                val sucesso = assertIs<TelaEstado.Sucesso<List<Campaign>>>(awaitItem().estadoLista)
                assertEquals("Campanha Recuperada", sucesso.dados.first().title)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun historicoViewModel_eventoTrocaDeAba_filtraFeitasERecebidas() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val doacaoFeita =
                Doacao(
                    id = "1",
                    itemTitle = "Cesta básica",
                    counterpartName = "ONG Luz",
                    direction = DoacaoDirecao.FEITA,
                    status = DoacaoStatus.CONCLUIDA,
                    dateLabel = "Hoje",
                )
            val fakeRepo =
                FakeDoacaoRepository(
                    respostaHistorico = Result.success(ResultadoCacheado(listOf(doacaoFeita))),
                )

            val viewModel = HistoricoViewModel(fakeRepo)

            viewModel.uiState.test {
                assertIs<TelaEstado.Carregando>(awaitItem().estadoLista)

                val estadoFeitas = awaitItem()
                assertEquals(HistoricoAba.FEITAS, estadoFeitas.abaSelecionada)
                val sucessoFeitas = assertIs<TelaEstado.Sucesso<List<Doacao>>>(estadoFeitas.estadoLista)
                assertEquals(1, sucessoFeitas.dados.size)

                // Evento: usuário troca para a aba RECEBIDAS (que não possui itens)
                viewModel.selecionarAba(HistoricoAba.RECEBIDAS)
                val estadoAba = awaitItem()
                assertEquals(HistoricoAba.RECEBIDAS, estadoAba.abaSelecionada)
                val estadoVazioRecebidas =
                    if (estadoAba.estadoLista is TelaEstado.Vazio) {
                        estadoAba
                    } else {
                        awaitItem()
                    }
                assertIs<TelaEstado.Vazio>(estadoVazioRecebidas.estadoLista)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun criarDoacaoViewModel_quandoSalvarFalha_mantemDadosDoFormularioNaTelaEMostraErro() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val fakeRepo =
                FakeDoacaoRepository(
                    respostaCriacao = Result.failure(FalhaDominio.SemConexao()),
                )

            val viewModel = CriarDoacaoViewModel(fakeRepo)

            viewModel.uiState.test {
                val inicial = awaitItem()
                assertIs<CriarDoacaoState.Idle>(inicial.submissionState)

                val formPreenchido =
                    DoacaoForm(
                        nome = "Agasalho Infantil",
                        quantidade = 3,
                        categoria = "Roupas",
                        eNovo = true,
                        descricao = "Agasalho novo na etiqueta",
                        motivo = "Doação de inverno",
                    )
                viewModel.atualizarForm(formPreenchido)
                assertEquals("Agasalho Infantil", awaitItem().form.nome)

                viewModel.submeterDoacao()
                assertIs<CriarDoacaoState.Loading>(awaitItem().submissionState)

                val estadoErro = awaitItem()
                val erro = assertIs<CriarDoacaoState.Error>(estadoErro.submissionState)
                assertTrue(erro.message.contains("Sem conexão", ignoreCase = true))
                // Requisito T6: quando salvar falhar, manter os dados na tela
                assertEquals("Agasalho Infantil", estadoErro.form.nome)
                assertEquals(3, estadoErro.form.quantidade)
                assertEquals("Agasalho novo na etiqueta", estadoErro.form.descricao)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
