package io.solidar.donation

import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus

internal val testCampaigns: List<Campaign> =
    listOf(
        Campaign(
            id = "1",
            title = "Cestas básicas comunitárias",
            organizer = "ONG Teste",
            goalInCents = 500_000L,
            raisedInCents = 312_500L,
            descricao = "Campanha de teste para arrecadação de alimentos.",
            itensNecessarios =
                listOf(
                    CampaignItem(id = "i1", nome = "Alimentos não perecíveis", quantidadeDescricao = "2 unidades", iconeRef = "food"),
                    CampaignItem(id = "i2", nome = "Materiais de higiene", quantidadeDescricao = "3 unidades", iconeRef = "hygiene"),
                ),
        ),
        Campaign(
            id = "2",
            title = "Reforma comunitária",
            organizer = "Associação Teste",
            goalInCents = 1_800_000L,
            raisedInCents = 240_000L,
            itensNecessarios =
                listOf(
                    CampaignItem(id = "i7", nome = "Telhas", quantidadeDescricao = "50 unidades", iconeRef = "home"),
                ),
        ),
    )

internal val testDoacoes: List<Doacao> =
    listOf(
        Doacao(
            id = "1",
            itemTitle = "Cesta básica",
            counterpartName = "ONG Teste",
            direction = DoacaoDirecao.FEITA,
            status = DoacaoStatus.CONCLUIDA,
            dateLabel = "12 de setembro de 2026",
        ),
        Doacao(
            id = "2",
            itemTitle = "Roupas de inverno",
            counterpartName = "Associação Teste",
            direction = DoacaoDirecao.FEITA,
            status = DoacaoStatus.PENDENTE,
            dateLabel = "19 de setembro de 2026",
        ),
        Doacao(
            id = "3",
            itemTitle = "Material escolar",
            counterpartName = "Maria da Silva",
            direction = DoacaoDirecao.RECEBIDA,
            status = DoacaoStatus.CONCLUIDA,
            dateLabel = "5 de setembro de 2026",
        ),
    )
