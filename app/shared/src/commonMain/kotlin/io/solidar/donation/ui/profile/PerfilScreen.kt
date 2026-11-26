package io.solidar.donation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado

internal val samplePerfilPf =
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

/**
 * Aba principal "Perfil" com visual moderno inspirado em redes sociais de impacto:
 * - Capa em gradiente com avatar sobreposto e anel de destaque;
 * - Handle, selo de verificação, bio de impacto e barra rápida de ações;
 * - Painel de métricas sociais (Doações feitas, Causas salvas e Recebidas);
 * - Cartão de nível de impacto na comunidade Solidar.io;
 * - Destaques de localização, credenciais e configurações de segurança.
 *
 * Trata os quatro estados obrigatórios da classe selada [TelaEstado] em um `when` exaustivo sem `else`.
 */
@Composable
fun PerfilScreen(
    estado: TelaEstado<PerfilUsuario>,
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
    onTentarDeNovo: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    mensagemFeedback: String? = null,
    totalDoacoesFeitas: Int = 2,
    totalDoacoesRecebidas: Int = 1,
    totalFavoritas: Int = 3,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (estado) {
            is TelaEstado.Carregando -> {
                Text(
                    text = "Meu Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                            .semantics { heading() },
                )
                EstadoCarregandoView(
                    descricao = "Carregando perfil do usuário",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Vazio -> {
                Text(
                    text = "Meu Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                            .semantics { heading() },
                )
                EmptyPerfilView(
                    onRecarregar = onTentarDeNovo,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Erro -> {
                Text(
                    text = "Meu Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                            .semantics { heading() },
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    EstadoErroView(
                        mensagem = estado.mensagem,
                        onTentarDeNovo = onTentarDeNovo,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Text("Sair da conta", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is TelaEstado.Sucesso -> {
                PerfilSocialContent(
                    perfil = estado.dados,
                    desatualizado = estado.desatualizado,
                    wide = wide,
                    mensagemFeedback = mensagemFeedback,
                    totalDoacoesFeitas = totalDoacoesFeitas,
                    totalDoacoesRecebidas = totalDoacoesRecebidas,
                    totalFavoritas = totalFavoritas,
                    onRecarregar = onTentarDeNovo,
                    onEditarPerfilClick = onEditarPerfilClick,
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun PerfilSocialContent(
    perfil: PerfilUsuario,
    desatualizado: Boolean,
    wide: Boolean,
    mensagemFeedback: String?,
    totalDoacoesFeitas: Int,
    totalDoacoesRecebidas: Int,
    totalFavoritas: Int,
    onRecarregar: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
    onLogout: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
        ) {
            // 1. Hero Header Social (Capa + Avatar Sobreposto + Bio + Botões Rápidos)
            SocialProfileHeroHeader(
                perfil = perfil,
                onEditarPerfilClick = onEditarPerfilClick,
                onAlterarSenhaClick = onAlterarSenhaClick,
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (desatualizado) {
                    AvisoDadosDesatualizadosBanner(
                        onRecarregar = onRecarregar,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (!mensagemFeedback.isNullOrBlank()) {
                    FeedbackSucessoBanner(mensagem = mensagemFeedback)
                }

                // 2. Barra de Métricas Sociais (Estilo Contadores de Rede Social)
                SocialImpactStatsCard(
                    doacoesFeitas = totalDoacoesFeitas,
                    causasSalvas = totalFavoritas,
                    doacoesRecebidas = totalDoacoesRecebidas,
                )

                // 3. Selo de Impacto & Reputação na Comunidade
                NivelComunidadeCard(
                    perfil = perfil,
                    totalInteracoes = totalDoacoesFeitas + totalFavoritas + totalDoacoesRecebidas,
                )

                // 4. Cartões de Detalhes ("Sobre & Localização" e "Segurança e Conta")
                if (wide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SobreLocalizacaoSocialCard(
                            perfil = perfil,
                            onEditarPerfilClick = onEditarPerfilClick,
                            modifier = Modifier.weight(1f),
                        )
                        SegurancaContaSocialCard(
                            onEditarPerfilClick = onEditarPerfilClick,
                            onAlterarSenhaClick = onAlterarSenhaClick,
                            onLogout = onLogout,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    SobreLocalizacaoSocialCard(
                        perfil = perfil,
                        onEditarPerfilClick = onEditarPerfilClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SegurancaContaSocialCard(
                        onEditarPerfilClick = onEditarPerfilClick,
                        onAlterarSenhaClick = onAlterarSenhaClick,
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun SocialProfileHeroHeader(
    perfil: PerfilUsuario,
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
) {
    val handle =
        perfil.email
            .substringBefore("@")
            .ifBlank { perfil.nome.lowercase().replace(" ", ".") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Capa com gradiente da paleta do tema e círculos decorativos translúcidos
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    ),
                            ),
                        ),
            ) {
                // Elementos circulares decorativos de fundo da capa
                Box(
                    modifier =
                        Modifier
                            .size(160.dp)
                            .offset(x = (-40).dp, y = (-30).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                            .clearAndSetSemantics {},
                )
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(140.dp)
                            .offset(x = 30.dp, y = 40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                            .clearAndSetSemantics {},
                )

                // Barra de título sobre a capa + Selo de perfil verificado
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Meu Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.semantics { heading() },
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = perfil.tipoExibicao,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Linha do Avatar Sobreposto + Botão Rápido estilo Rede Social
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 98.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                // Avatar com anel de borda da cor do fundo da tela
                Box(
                    modifier =
                        Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                            .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = perfil.iniciais,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                // Ações rápidas ao lado do avatar (estilo perfil de rede social)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onAlterarSenhaClick,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(50),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Senha",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Informações de Nome, @handle, Bio e Botão Principal de Edição
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = perfil.nome,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Text(
                text = "@$handle • ${perfil.email}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text =
                    if (perfil.isInstituicao) {
                        "Organização social conectada à rede Solidar.io para mobilizar campanhas e arrecadações."
                    } else {
                        "Apoiando causas sociais e compartilhando solidariedade na comunidade Solidar.io."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
            )

            // Chips de localização rápida e documento na bio (estilo Twitter/Instagram)
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text =
                                if (perfil.bairro.isNotBlank()) {
                                    perfil.bairro
                                } else {
                                    "Local não definido"
                                },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                if (perfil.documento.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        Text(
                            text = perfil.documentoFormatado,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onEditarPerfilClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text(
                    text = "Editar dados do perfil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun SocialImpactStatsCard(
    doacoesFeitas: Int,
    causasSalvas: Int,
    doacoesRecebidas: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumnItem(
                valor = "$doacoesFeitas",
                rotulo = "Doações feitas",
                destaquePrimario = true,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            StatColumnItem(
                valor = "$causasSalvas",
                rotulo = "Causas salvas",
                destaquePrimario = false,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            StatColumnItem(
                valor = "$doacoesRecebidas",
                rotulo = "Recebidas",
                destaquePrimario = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumnItem(
    valor: String,
    rotulo: String,
    destaquePrimario: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color =
                if (destaquePrimario) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NivelComunidadeCard(
    perfil: PerfilUsuario,
    totalInteracoes: Int,
    modifier: Modifier = Modifier,
) {
    val progressoNivel = ((totalInteracoes.coerceAtLeast(1) % 10) / 10f).coerceIn(0.25f, 1f)
    val percentualNivel = (progressoNivel * 100).toInt()
    val tituloNivel =
        when {
            perfil.isInstituicao -> "Instituição Verificada na Rede"
            totalInteracoes >= 5 -> "Embaixador Solidário"
            else -> "Membro Solidário Ativo"
        }

    Card(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = tituloNivel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "Nível de engajamento na comunidade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = "$percentualNivel%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progressoNivel },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .semantics {
                            contentDescription = "Progresso de engajamento: $percentualNivel por cento"
                        },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun SobreLocalizacaoSocialCard(
    perfil: PerfilUsuario,
    onEditarPerfilClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Meus dados e endereço",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            InfoDetailRow(
                icon = AppIcons.Home,
                titulo = "Ponto de entrega / endereço",
                valor = perfil.enderecoFormatado,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            InfoDetailRow(
                icon = AppIcons.Email,
                titulo = "E-mail vinculado",
                valor = perfil.email,
            )

            if (perfil.documento.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                InfoDetailRow(
                    icon = AppIcons.Person,
                    titulo = if (perfil.isInstituicao) "CNPJ cadastrado" else "CPF cadastrado",
                    valor = perfil.documentoFormatado,
                )
            }

            OutlinedButton(
                onClick = onEditarPerfilClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text(
                    text = "Atualizar endereço e nome",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InfoDetailRow(
    icon: ImageVector,
    titulo: String,
    valor: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SegurancaContaSocialCard(
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Segurança e conta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            // Item de ação estilo menu social para Editar Perfil
            SocialMenuActionRow(
                icon = AppIcons.Person,
                titulo = "Dados cadastrais",
                subtitulo = "Alterar nome de exibição, CEP e endereço",
                onClick = onEditarPerfilClick,
            )

            // Item de ação para Alterar Senha
            OutlinedButton(
                onClick = onAlterarSenhaClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Icon(
                    imageVector = AppIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alterar senha",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
            ) {
                Text(
                    text = "Sair da conta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun SocialMenuActionRow(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeedbackSucessoBanner(mensagem: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun EmptyPerfilView(
    onRecarregar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Nenhuma informação de perfil disponível no momento.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRecarregar,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Recarregar", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Sair da conta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PerfilScreenFilledPreview() {
    DonationTheme {
        PerfilScreen(
            estado = TelaEstado.Sucesso(samplePerfilPf),
            onEditarPerfilClick = {},
            onAlterarSenhaClick = {},
            onTentarDeNovo = {},
            onLogout = {},
            mensagemFeedback = "Dados do perfil atualizados com sucesso!",
        )
    }
}

@Preview
@Composable
private fun PerfilScreenEmptyPreview() {
    DonationTheme {
        PerfilScreen(
            estado = TelaEstado.Vazio,
            onEditarPerfilClick = {},
            onAlterarSenhaClick = {},
            onTentarDeNovo = {},
            onLogout = {},
        )
    }
}

@Preview
@Composable
private fun PerfilScreenLoadingPreview() {
    DonationTheme {
        PerfilScreen(
            estado = TelaEstado.Carregando,
            onEditarPerfilClick = {},
            onAlterarSenhaClick = {},
            onTentarDeNovo = {},
            onLogout = {},
        )
    }
}

@Preview
@Composable
private fun PerfilScreenDarkPreview() {
    DonationTheme(darkTheme = true) {
        PerfilScreen(
            estado = TelaEstado.Sucesso(samplePerfilPf, desatualizado = true),
            onEditarPerfilClick = {},
            onAlterarSenhaClick = {},
            onTentarDeNovo = {},
            onLogout = {},
        )
    }
}

@Preview(widthDp = 900, heightDp = 600)
@Composable
private fun PerfilScreenWidePreview() {
    DonationTheme {
        PerfilScreen(
            estado = TelaEstado.Sucesso(samplePerfilPf),
            onEditarPerfilClick = {},
            onAlterarSenhaClick = {},
            onTentarDeNovo = {},
            onLogout = {},
            wide = true,
        )
    }
}
