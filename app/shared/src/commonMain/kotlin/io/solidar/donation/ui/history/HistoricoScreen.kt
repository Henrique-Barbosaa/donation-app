package io.solidar.donation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.DoacaoCard

/** As duas abas do histórico, pedidas na issue #11. */
enum class HistoricoAba {
    FEITAS,
    RECEBIDAS,
}

/**
 * Tela de histórico de doações: "doações que eu fiz" e "que eu recebi",
 * separadas por abas.
 *
 * Segue o mesmo princípio da [io.solidar.donation.ui.home.HomeScreen]: sem
 * `remember` aqui dentro. A aba selecionada e a lista completa vêm de fora,
 * e a filtragem por direção é só uma computação pura — não é estado
 * guardado pela tela.
 */
@Composable
fun HistoricoScreen(
    doacoes: List<Doacao>,
    selectedTab: HistoricoAba,
    onTabSelected: (HistoricoAba) -> Unit,
    modifier: Modifier = Modifier,
) {
    val direcaoDaAba =
        when (selectedTab) {
            HistoricoAba.FEITAS -> DoacaoDirecao.FEITA
            HistoricoAba.RECEBIDAS -> DoacaoDirecao.RECEBIDA
        }
    val doacoesFiltradas = doacoes.filter { it.direction == direcaoDaAba }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Histórico de doações",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == HistoricoAba.FEITAS,
                onClick = { onTabSelected(HistoricoAba.FEITAS) },
                text = { Text("Feitas") },
            )
            Tab(
                selected = selectedTab == HistoricoAba.RECEBIDAS,
                onClick = { onTabSelected(HistoricoAba.RECEBIDAS) },
                text = { Text("Recebidas") },
            )
        }

        if (doacoesFiltradas.isEmpty()) {
            EmptyHistoricoMessage(selectedTab = selectedTab, modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = doacoesFiltradas, key = { it.id }) { doacao ->
                    DoacaoCard(doacao = doacao)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoricoMessage(
    selectedTab: HistoricoAba,
    modifier: Modifier = Modifier,
) {
    val message =
        when (selectedTab) {
            HistoricoAba.FEITAS -> "Você ainda não fez nenhuma doação.\nQuando fizer, ela aparece aqui."
            HistoricoAba.RECEBIDAS -> "Você ainda não recebeu nenhuma doação.\nQuando receber, ela aparece aqui."
        }
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HistoricoScreenFilledPreview() {
    DonationTheme {
        HistoricoScreen(
            doacoes = sampleDoacoes,
            selectedTab = HistoricoAba.FEITAS,
            onTabSelected = {},
        )
    }
}

@Preview
@Composable
private fun HistoricoScreenEmptyPreview() {
    DonationTheme {
        HistoricoScreen(
            doacoes = emptyList(),
            selectedTab = HistoricoAba.RECEBIDAS,
            onTabSelected = {},
        )
    }
}
