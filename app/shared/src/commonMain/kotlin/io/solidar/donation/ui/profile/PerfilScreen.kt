package io.solidar.donation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado

/**
 * Aba principal "Perfil" com layout moderno inspirado em perfis sociais, exibindo
 * exclusivamente os dados reais existentes no backend (`USUARIO`, `PESSOA_FISICA`, `INSTITUICAO` e `DOACAO`):
 * - Cabeçalho com avatar de iniciais, nome, e-mail, tipo de conta e contadores reais de doações;
 * - Barra dupla de ações rápidas ("Editar Dados" e "Alterar senha");
 * - Cartão de credenciais cadastradas no banco (CPF/CNPJ, Data de Nascimento ou Site, E-mail);
 * - Cartão estruturado com os campos reais de endereço (Logradouro, Número, Bairro e CEP).
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
    totalDoacoesFeitas: Int = 0,
    totalDoacoesRecebidas: Int = 0,
    totalFavoritas: Int = 0,
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
                    totalDoacoesFeitas = totalDoacoesFeitas,
                    totalDoacoesRecebidas = totalDoacoesRecebidas,
                    totalFavoritas = totalFavoritas,
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
    totalDoacoesFeitas: Int,
    totalDoacoesRecebidas: Int,
    totalFavoritas: Int,
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
                    .widthIn(max = 860.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!mensagemFeedback.isNullOrBlank()) {
                FeedbackSucessoBanner(mensagem = mensagemFeedback)
            }

            // 1. Cabeçalho Moderno estilo Perfil Social (Iniciais + Contadores Reais + Botões de Ação)
            PerfilHeaderSocialCard(
                perfil = perfil,
                doacoesFeitas = totalDoacoesFeitas,
                doacoesRecebidas = totalDoacoesRecebidas,
                campanhasFavoritas = totalFavoritas,
                onEditarPerfilClick = onEditarPerfilClick,
                onAlterarSenhaClick = onAlterarSenhaClick,
            )

            // 2. Cartões de Dados Reais do Banco (Credenciais + Endereço)
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DadosCadastraisCard(
                        perfil = perfil,
                        modifier = Modifier.weight(1f),
                    )
                    EnderecoCadastradoCard(
                        perfil = perfil,
                        onEditarPerfilClick = onEditarPerfilClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                DadosCadastraisCard(
                    perfil = perfil,
                    modifier = Modifier.fillMaxWidth(),
                )
                EnderecoCadastradoCard(
                    perfil = perfil,
                    onEditarPerfilClick = onEditarPerfilClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 3. Ação de Encerramento de Sessão
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Segurança e conta",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = "Encerrar sessão atual neste dispositivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.heightIn(min = 48.dp),
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
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PerfilHeaderSocialCard(
    perfil: PerfilUsuario,
    doacoesFeitas: Int,
    doacoesRecebidas: Int,
    campanhasFavoritas: Int,
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Topo estilo Rede Social: Avatar à esquerda + Métricas reais à direita
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(76.dp)
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
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                // Contadores reais (Doações feitas, Recebidas, Favoritas)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatColumnItem(
                        valor = "$doacoesFeitas",
                        rotulo = "Doações",
                        modifier = Modifier.weight(1f),
                    )
                    StatColumnItem(
                        valor = "$doacoesRecebidas",
                        rotulo = "Recebidas",
                        modifier = Modifier.weight(1f),
                    )
                    StatColumnItem(
                        valor = "$campanhasFavoritas",
                        rotulo = "Favoritas",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Nome, E-mail e Badge do tipo de conta (Pessoa Física / Instituição)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = perfil.nome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).semantics { heading() },
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(
                            text = perfil.tipoExibicao,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = perfil.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Barra de botões de ação rápida lado a lado (estilo "Edit Profile / Security" de apps sociais)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onEditarPerfilClick,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text(
                        text = "Editar Dados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                OutlinedButton(
                    onClick = onAlterarSenhaClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
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
                        text = "Alterar senha",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatColumnItem(
    valor: String,
    rotulo: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DadosCadastraisCard(
    perfil: PerfilUsuario,
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
                text = "Informações da conta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            InfoDetailRow(
                icon = AppIcons.Person,
                titulo = if (perfil.isInstituicao) "CNPJ" else "CPF",
                valor =
                    perfil.documentoFormatado.ifBlank {
                        "Não informado"
                    },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            InfoDetailRow(
                icon = AppIcons.Email,
                titulo = "E-mail",
                valor = perfil.email.ifBlank { "Não informado" },
            )

            if (perfil.isInstituicao && !perfil.site.isNullOrBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                InfoDetailRow(
                    icon = AppIcons.Search,
                    titulo = "Site da instituição",
                    valor = perfil.site,
                )
            } else if (!perfil.isInstituicao && !perfil.dataNascimento.isNullOrBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                InfoDetailRow(
                    icon = AppIcons.Person,
                    titulo = "Data de nascimento",
                    valor = perfil.dataNascimento,
                )
            }
        }
    }
}

@Composable
private fun EnderecoCadastradoCard(
    perfil: PerfilUsuario,
    onEditarPerfilClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cepDigitos = perfil.cep.filter { it.isDigit() }
    val cepFormatado =
        if (cepDigitos.length == 8) {
            "${cepDigitos.substring(0, 5)}-${cepDigitos.substring(5, 8)}"
        } else {
            perfil.cep
        }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Meus dados e endereço",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color =
                        if (perfil.possuiEndereco) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    contentColor =
                        if (perfil.possuiEndereco) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                ) {
                    Text(
                        text = if (perfil.possuiEndereco) "Cadastrado" else "Pendente",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            if (perfil.possuiEndereco) {
                InfoDetailRow(
                    icon = AppIcons.Home,
                    titulo = "Logradouro e número",
                    valor =
                        listOfNotNull(
                            perfil.logradouro.takeIf { it.isNotBlank() },
                            perfil.numero.takeIf { it.isNotBlank() }?.let { "nº $it" },
                        ).joinToString(", ").ifBlank { "Não informado" },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Bairro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = perfil.bairro.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CEP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = cepFormatado.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Nenhum endereço cadastrado ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Adicione seu CEP, rua e bairro para facilitar a entrega e coleta de doações.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = onEditarPerfilClick,
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Cadastrar endereço", fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
        verticalAlignment = Alignment.CenterVertically,
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
