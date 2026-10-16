package io.solidar.donation.ui.home

import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem

/**
 * Dados de exemplo usados nos previews e na Home enquanto o backend não
 * expõe o endpoint de campanhas.
 *
 * TODO: remover quando a Home passar a consumir a API (`GET /campaigns`).
 */
internal val sampleCampaigns: List<Campaign> =
    listOf(
        Campaign(
            id = "1",
            title = "Cestas básicas para o bairro das Rocas",
            organizer = "Instituto Mãos Solidárias",
            goalInCents = 500_000L,
            raisedInCents = 312_500L,
            descricao = "Esta campanha visa apoiar 50 famílias vulneráveis no bairro das Rocas que enfrentam insegurança alimentar extrema. Com a sua doação de cestas básicas ou contribuição financeira, garantimos alimentação e dignidade para as nossas crianças e idosos este mês. Junte-se a nós nesta corrente de solidariedade!",
            imageUrl = "https://images.unsplash.com/photo-1593113598332-cd288d649433?q=80&w=600", // Foto de doação de alimentos
            itensNecessarios = listOf(
                CampaignItem(id = "i1", nome = "Alimentos não perecíveis", quantidadeDescricao = "2 quantidade", iconeRef = "food"),
                CampaignItem(id = "i2", nome = "Materiais de higiene", quantidadeDescricao = "3 quantidade", iconeRef = "hygiene"),
                CampaignItem(id = "i3", nome = "Alimentos perecíveis", quantidadeDescricao = "1 item", iconeRef = "food_apple"),
                CampaignItem(id = "i4", nome = "Itens de Casa", quantidadeDescricao = "2 quantidade", iconeRef = "home"),
                CampaignItem(id = "i5", nome = "Roupas e Casacos", quantidadeDescricao = "1 item", iconeRef = "check"),
                CampaignItem(id = "i6", nome = "Produtos de Limpeza", quantidadeDescricao = "2 quantidade", iconeRef = "hygiene")
            )
        ),
        Campaign(
            id = "2",
            title = "Reforma do telhado da creche São Judas",
            organizer = "Associação de Moradores de Felipe Camarão",
            goalInCents = 1_800_000L,
            raisedInCents = 240_000L,
            itensNecessarios = listOf(
                CampaignItem(id = "i7", nome = "Telhas Brasilit", quantidadeDescricao = "50 unidades", iconeRef = "home"),
                CampaignItem(id = "i8", nome = "Sacos de Cimento", quantidadeDescricao = "10 sacos", iconeRef = "check")
            )
        ),
        Campaign(
            id = "3",
            title = "Material escolar para 120 crianças",
            organizer = "ONG Semear",
            goalInCents = 350_000L,
            raisedInCents = 350_000L,
            itensNecessarios = listOf(
                CampaignItem(id = "i9", nome = "Cadernos 10 matérias", quantidadeDescricao = "120 unidades", iconeRef = "check"),
                CampaignItem(id = "i10", nome = "Estojos completos", quantidadeDescricao = "120 unidades", iconeRef = "check")
            )
        ),
    )