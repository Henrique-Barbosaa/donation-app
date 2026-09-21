package io.solidar.donation.ui.history

import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus

/**
 * Dados de exemplo para a tela de histórico, usados nos previews e
 * enquanto o backend não expõe o endpoint de histórico do usuário.
 *
 * TODO: remover quando existir o endpoint (ex.: `GET /doacoes/historico`).
 */
internal val sampleDoacoes: List<Doacao> = listOf(
    Doacao(
        id = "1",
        itemTitle = "Cesta básica",
        counterpartName = "Instituto Mãos Solidárias",
        direction = DoacaoDirecao.FEITA,
        status = DoacaoStatus.CONCLUIDA,
        dateLabel = "12 de setembro de 2026"
    ),
    Doacao(
        id = "2",
        itemTitle = "Roupas de inverno",
        counterpartName = "Associação de Moradores de Felipe Camarão",
        direction = DoacaoDirecao.FEITA,
        status = DoacaoStatus.PENDENTE,
        dateLabel = "19 de setembro de 2026"
    ),
    Doacao(
        id = "3",
        itemTitle = "Material escolar",
        counterpartName = "Maria da Silva",
        direction = DoacaoDirecao.RECEBIDA,
        status = DoacaoStatus.CONCLUIDA,
        dateLabel = "5 de setembro de 2026"
    )
)