package io.solidar.donation.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.DemonstrarInteresseDialog
import io.solidar.donation.ui.components.ImagemRemota

private const val PROPORCAO_IMAGEM = 4f / 3f

/**
 * Detalhe de uma doação disponível (aba Explorar): foto, categoria, condição,
 * descrição, motivo e a ação de manifestar interesse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoacaoDetalheScreen(
    doacao: DoacaoDisponivel?,
    carregando: Boolean,
    interessado: Boolean,
    processando: Boolean,
    ehCriador: Boolean,
    aviso: String?,
    onBack: () -> Unit,
    onAlternarInteresse: (String) -> Unit,
    onRegistrarInteresseComMensagem: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    if (mostrarDialogo && doacao != null) {
        DemonstrarInteresseDialog(
            doacao = doacao,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { mensagem ->
                mostrarDialogo = false
                onRegistrarInteresseComMensagem(doacao.id, mensagem)
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalhe da doação", modifier = Modifier.semantics { heading() }) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        bottomBar = {
            if (doacao != null) {
                BarraInteresse(
                    doacao = doacao,
                    interessado = interessado,
                    processando = processando,
                    ehCriador = ehCriador,
                    aviso = aviso,
                    onRemoverInteresse = { onAlternarInteresse(doacao.id) },
                    onDemonstrarInteresse = { mostrarDialogo = true },
                )
            }
        },
    ) { padding ->
        if (doacao != null) {
            DetalheConteudo(doacao = doacao, modifier = Modifier.padding(padding))
        } else {
            EstadoSemDoacao(carregando = carregando, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun EstadoSemDoacao(
    carregando: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (carregando) {
            CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = "Carregando doação" })
        } else {
            Text(
                text = "Esta doação não está mais disponível.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun DetalheConteudo(
    doacao: DoacaoDisponivel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!doacao.urlImagem.isNullOrBlank()) {
            ImagemRemota(
                caminho = doacao.urlImagem,
                contentDescription = "Foto de ${doacao.itemTitle}",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(PROPORCAO_IMAGEM)
                        .clip(RoundedCornerShape(16.dp)),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta(texto = doacao.categoria, primaria = true)
            Etiqueta(texto = "${doacao.condicao} • ${doacao.quantidade} un.", primaria = false)
        }
        Text(
            text = doacao.itemTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Doador: ${doacao.nomeDoador}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SecaoTexto(titulo = "Descrição", texto = doacao.descricao)
        SecaoTexto(titulo = "Motivo da doação", texto = doacao.motivo)
    }
}

@Composable
private fun Etiqueta(
    texto: String,
    primaria: Boolean,
) {
    val fundo =
        if (primaria) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    val conteudo =
        if (primaria) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
    Surface(shape = RoundedCornerShape(50), color = fundo) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = conteudo,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SecaoTexto(
    titulo: String,
    texto: String,
) {
    if (texto.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BarraInteresse(
    doacao: DoacaoDisponivel,
    interessado: Boolean,
    processando: Boolean,
    ehCriador: Boolean,
    aviso: String?,
    onRemoverInteresse: () -> Unit,
    onDemonstrarInteresse: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!aviso.isNullOrBlank()) {
                Text(
                    text = aviso,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            val modificadorBotao = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            when {
                ehCriador -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = modificadorBotao,
                    ) {
                        Text(
                            text = "Sua oferta de doação",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
                interessado -> {
                    OutlinedButton(
                        onClick = onRemoverInteresse,
                        enabled = !processando,
                        shape = RoundedCornerShape(12.dp),
                        modifier =
                            modificadorBotao.semantics {
                                contentDescription = "Remover interesse em ${doacao.itemTitle}"
                            },
                    ) {
                        RotuloBotao(processando = processando, texto = "Interesse registrado")
                    }
                }
                else -> {
                    Button(
                        onClick = onDemonstrarInteresse,
                        enabled = !processando,
                        shape = RoundedCornerShape(12.dp),
                        modifier =
                            modificadorBotao.semantics {
                                contentDescription = "Demonstrar interesse em ${doacao.itemTitle}"
                            },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        RotuloBotao(processando = processando, texto = "Tenho interesse")
                    }
                }
            }
        }
    }
}

@Composable
private fun RotuloBotao(
    processando: Boolean,
    texto: String,
) {
    if (processando) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current,
        )
    } else {
        Text(text = texto, fontWeight = FontWeight.Bold)
    }
}
