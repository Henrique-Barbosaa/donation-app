package io.solidar.donation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Aba principal "Perfil" da barra de navegação, exibindo os dados reais do usuário autenticado,
 * resumo de endereço e atalhos para editar dados cadastrais, alterar senha ou encerrar sessão.
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
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
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

        when (estado) {
            is TelaEstado.Carregando -> {
                EstadoCarregandoView(
                    descricao = "Carregando perfil do usuário",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Vazio -> {
                EmptyPerfilView(
                    onRecarregar = onTentarDeNovo,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Erro -> {
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
                if (estado.desatualizado) {
                    AvisoDadosDesatualizadosBanner(onRecarregar = onTentarDeNovo)
                }
                PerfilSucessoContent(
                    perfil = estado.dados,
                    wide = wide,
                    mensagemFeedback = mensagemFeedback,
                    onEditarPerfilClick = onEditarPerfilClick,
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun PerfilSucessoContent(
    perfil: PerfilUsuario,
    wide: Boolean,
    mensagemFeedback: String?,
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
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!mensagemFeedback.isNullOrBlank()) {
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
                            text = mensagemFeedback,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Card 1: Cabeçalho de Identidade
            PerfilIdentidadeCard(perfil = perfil)

            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PerfilEnderecoCard(
                        perfil = perfil,
                        onEditarPerfilClick = onEditarPerfilClick,
                        modifier = Modifier.weight(1f),
                    )
                    PerfilSegurancaCard(
                        onAlterarSenhaClick = onAlterarSenhaClick,
                        onLogout = onLogout,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                PerfilEnderecoCard(
                    perfil = perfil,
                    onEditarPerfilClick = onEditarPerfilClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                PerfilSegurancaCard(
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun PerfilIdentidadeCard(
    perfil: PerfilUsuario,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = perfil.iniciais,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = perfil.nome,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )

            Text(
                text = perfil.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = perfil.tipoExibicao,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerfilEnderecoCard(
    perfil: PerfilUsuario,
    onEditarPerfilClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Meus dados e endereço",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Endereço de referência",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = perfil.enderecoFormatado,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight =
                            if (perfil.possuiEndereco) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            },
                        color =
                            if (perfil.possuiEndereco) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }

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
private fun PerfilSegurancaCard(
    onAlterarSenhaClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Segurança e conta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Text(
                text = "Mantenha sua senha atualizada ou encerre a sessão atual neste dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedButton(
                onClick = onAlterarSenhaClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
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
