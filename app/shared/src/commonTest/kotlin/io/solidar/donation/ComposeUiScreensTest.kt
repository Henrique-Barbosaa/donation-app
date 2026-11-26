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
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.AuthState
import io.solidar.donation.ui.auth.LoginContent
import io.solidar.donation.ui.campaign.CampanhaDetalheScreen
import io.solidar.donation.ui.donation.CriarDoacaoContent
import io.solidar.donation.ui.donation.CriarDoacaoState
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.sampleDoacoes
import io.solidar.donation.ui.home.HomeScreen
import io.solidar.donation.ui.home.sampleCampaigns
import io.solidar.donation.ui.profile.AlterarSenhaContent
import io.solidar.donation.ui.profile.EditarPerfilContent
import io.solidar.donation.ui.profile.PerfilScreen
import io.solidar.donation.ui.profile.SubmissaoPerfilState
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.util.AlterarSenhaForm
import io.solidar.donation.util.DoacaoForm
import io.solidar.donation.util.PerfilForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ComposeUiScreensTest {
    @Test
    fun homeScreen_exibeCampanhasNoEstadoSucessoEAvisoQuandoDesatualizado() =
        runComposeUiTest {
            setContent {
                DonationTheme {
                    HomeScreen(
                        estado =
                            TelaEstado.Sucesso(
                                dados = sampleCampaigns,
                                desatualizado = true,
                            ),
                        onCampaignClick = {},
                        onTentarDeNovo = {},
                    )
                }
            }

            onNodeWithText("Campanhas abertas").assertIsDisplayed()
            onNodeWithText(sampleCampaigns.first().title).assertIsDisplayed()
            onNodeWithText(
                "Modo offline: exibindo últimos dados salvos (podem estar desatualizados).",
            ).assertIsDisplayed()
        }

    @Test
    fun homeScreen_estadosVazioEErro_exibemMensagemEBotaoTentarDeNovo() =
        runComposeUiTest {
            var estadoAtual by mutableStateOf<TelaEstado<List<Campaign>>>(
                TelaEstado.Erro("Sem conexão com o servidor. Verifique sua internet."),
            )
            var tentativas = 0

            setContent {
                DonationTheme {
                    HomeScreen(
                        estado = estadoAtual,
                        onCampaignClick = {},
                        onTentarDeNovo = {
                            tentativas++
                            estadoAtual = TelaEstado.Vazio
                        },
                    )
                }
            }

            onNodeWithText("Sem conexão com o servidor. Verifique sua internet.").assertIsDisplayed()
            onNodeWithText("Tentar de novo").assertIsDisplayed().performClick()

            assertEquals(1, tentativas)
            onNodeWithText(
                "Nenhuma campanha por aqui ainda.\nAssim que alguém criar uma, ela aparece nesta lista.",
            ).assertIsDisplayed()
        }

    @Test
    fun criarDoacaoFormulario_invalidoDesabilitaBotao_validoHabilitaESubmete() =
        runComposeUiTest {
            var form by mutableStateOf(DoacaoForm(nome = "Oi"))
            var submetido = false

            setContent {
                DonationTheme {
                    CriarDoacaoContent(
                        state = CriarDoacaoState.Idle,
                        form = form,
                        onFormChange = { form = it },
                        onNavigateBack = {},
                        onSubmit = { submetido = true },
                    )
                }
            }

            // Caso inválido: nome com menos de 3 caracteres mostra erro e desabilita o botão
            onNodeWithText("Use pelo menos 3 caracteres.").assertIsDisplayed()
            onNodeWithText("Publicar Doação").performScrollTo().assertIsNotEnabled()

            // Caso válido: atualiza para um nome válido e clica em Publicar Doação
            form = form.copy(nome = "Cesta Básica Completa")
            waitForIdle()

            onNodeWithText("Publicar Doação").performScrollTo().assertIsEnabled().performClick()
            assertTrue(submetido)
        }

    @Test
    fun loginScreen_validaFormularioENavegaParaCadastro() =
        runComposeUiTest {
            var email by mutableStateOf("")
            var senha by mutableStateOf("")
            var navegouParaCadastro = false

            setContent {
                DonationTheme {
                    LoginContent(
                        email = email,
                        onEmailChange = { email = it },
                        password = senha,
                        onPasswordChange = { senha = it },
                        loginState = AuthState.Idle,
                        onLogin = {},
                        onNavigateToSignUp = { navegouParaCadastro = true },
                    )
                }
            }

            onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
            onNodeWithText("Entrar").assertIsNotEnabled()

            onNodeWithText("E-mail").performTextInput("doador@solidar.io")
            onNodeWithText("Senha").performTextInput("123456")
            waitForIdle()

            onNodeWithText("Entrar").assertIsEnabled()
            onNodeWithText("Cadastre-se").performClick()
            assertTrue(navegouParaCadastro)
        }

    @Test
    fun historicoScreen_alternaEntreAbasFeitasERecebidas() =
        runComposeUiTest {
            var abaAtual by mutableStateOf(HistoricoAba.FEITAS)

            setContent {
                DonationTheme {
                    HistoricoScreen(
                        doacoes = sampleDoacoes,
                        selectedTab = abaAtual,
                        onTabSelected = { abaAtual = it },
                    )
                }
            }

            onNodeWithText("Histórico de doações").assertIsDisplayed()
            onNodeWithText("Cesta básica", substring = true).assertIsDisplayed()

            onNodeWithText("Recebidas").performClick()
            waitForIdle()

            assertEquals(HistoricoAba.RECEBIDAS, abaAtual)
            onNodeWithText("Material escolar", substring = true).assertIsDisplayed()
        }

    @Test
    fun navegacaoEntreFeedEDetalheDaCampanha_funcionaComAcessibilidade() =
        runComposeUiTest {
            var campanhaSelecionada by mutableStateOf<Campaign?>(null)
            var favoritada by mutableStateOf(false)

            setContent {
                DonationTheme {
                    val atual = campanhaSelecionada
                    if (atual == null) {
                        HomeScreen(
                            estado = TelaEstado.Sucesso(sampleCampaigns),
                            onCampaignClick = { campanhaSelecionada = it },
                            onTentarDeNovo = {},
                        )
                    } else {
                        CampanhaDetalheScreen(
                            campaign = atual,
                            isFavorito = favoritada,
                            onToggleFavorito = { favoritada = !favoritada },
                            onBack = { campanhaSelecionada = null },
                            onQueroDoar = {},
                        )
                    }
                }
            }

            // Clica no primeiro cartão de campanha da Home
            onNodeWithText(sampleCampaigns.first().title).performClick()
            waitForIdle()

            // Verifica que abriu a tela de detalhes da campanha
            onNodeWithText("Sobre a campanha:").assertIsDisplayed()
            onNodeWithContentDescription("Adicionar campanha aos favoritos").performClick()
            waitForIdle()
            onNodeWithContentDescription("Remover campanha dos favoritos").assertIsDisplayed()

            // Volta para a Home usando a descrição de acessibilidade do botão Voltar
            onNodeWithContentDescription("Voltar").performClick()
            waitForIdle()
            onNodeWithText("Campanhas abertas").assertIsDisplayed()
        }

    @Test
    fun perfilEEdicao_exibeDadosValidaFormulariosENavegaParaEdicaoESenha() =
        runComposeUiTest {
            val perfilTeste =
                PerfilUsuario(
                    id = 1,
                    nome = "Antonio Henrique Lima",
                    email = "henrique@solidar.io",
                    tipo = "PESSOA_FISICA",
                    documento = "12345678901",
                    logradouro = "Av. Senador Salgado Filho",
                    numero = "3000",
                    bairro = "Lagoa Nova",
                    cep = "59078970",
                )
            var telaAtual by mutableStateOf("PERFIL")
            var formPerfil by mutableStateOf(
                PerfilForm(
                    nome = "AB",
                    cep = "123",
                ),
            )
            var formSenha by mutableStateOf(
                AlterarSenhaForm(
                    senhaAtual = "123456",
                    novaSenha = "nova123",
                    confirmarNovaSenha = "diferente",
                ),
            )
            var perfilSalvo = false
            var senhaSalva = false

            setContent {
                DonationTheme {
                    when (telaAtual) {
                        "PERFIL" ->
                            PerfilScreen(
                                estado = TelaEstado.Sucesso(perfilTeste),
                                onEditarPerfilClick = { telaAtual = "EDITAR" },
                                onAlterarSenhaClick = { telaAtual = "SENHA" },
                                onTentarDeNovo = {},
                                onLogout = {},
                            )
                        "EDITAR" ->
                            EditarPerfilContent(
                                form = formPerfil,
                                onFormChange = { formPerfil = it },
                                submissaoState = SubmissaoPerfilState.Idle,
                                onNavigateBack = { telaAtual = "PERFIL" },
                                onSubmit = { perfilSalvo = true },
                            )
                        else ->
                            AlterarSenhaContent(
                                form = formSenha,
                                onFormChange = { formSenha = it },
                                submissaoState = SubmissaoPerfilState.Idle,
                                onNavigateBack = { telaAtual = "PERFIL" },
                                onSubmit = { senhaSalva = true },
                            )
                    }
                }
            }

            // 1. Verifica tela de Perfil e navega para Editar Perfil
            onNodeWithText("Meu Perfil").assertIsDisplayed()
            onNodeWithText(perfilTeste.nome, substring = true).assertIsDisplayed()
            onNodeWithText("Editar Dados").performScrollTo().performClick()
            waitForIdle()

            // 2. Formulário de Editar Perfil inválido -> botão desabilitado
            onNodeWithText("O nome deve ter no mínimo 3 caracteres.").assertIsDisplayed()
            onNodeWithText("O CEP deve conter 8 dígitos.").assertIsDisplayed()
            onNodeWithText("Salvar alterações").performScrollTo().assertIsNotEnabled()

            // Corrige formulário de Editar Perfil -> botão habilitado
            formPerfil =
                PerfilForm(
                    nome = "Antonio Henrique Barbosa",
                    cep = "59078970",
                    logradouro = "Av. Senador Salgado Filho",
                    numero = "3000",
                    bairro = "Lagoa Nova",
                )
            waitForIdle()
            onNodeWithText("Salvar alterações").performScrollTo().assertIsEnabled().performClick()
            assertTrue(perfilSalvo)

            // 3. Volta para o Perfil e abre Alterar Senha
            onNodeWithContentDescription("Voltar").performClick()
            waitForIdle()
            onNodeWithText("Alterar senha").performScrollTo().performClick()
            waitForIdle()

            // Confirmação divergente desabilita botão
            onNodeWithText("A confirmação não coincide com a nova senha.").assertIsDisplayed()
            onNodeWithText("Atualizar senha").performScrollTo().assertIsNotEnabled()

            // Corrige confirmação -> habilita e submete
            formSenha = formSenha.copy(confirmarNovaSenha = "nova123")
            waitForIdle()
            onNodeWithText("Atualizar senha").performScrollTo().assertIsEnabled().performClick()
            assertTrue(senhaSalva)
        }
}
