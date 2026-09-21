package io.solidar.donation.ui.home

import io.solidar.donation.model.Campaign

/**
 * Dados de exemplo usados nos previews e na Home enquanto o backend não
 * expõe o endpoint de campanhas.
 *
 * TODO: remover quando a Home passar a consumir a API (`GET /campaigns`).
 */
internal val sampleCampaigns: List<Campaign> = listOf(
    Campaign(
        id = "1",
        title = "Cestas básicas para o bairro das Rocas",
        organizer = "Instituto Mãos Solidárias",
        goalInCents = 500_000L,
        raisedInCents = 312_500L
    ),
    Campaign(
        id = "2",
        title = "Reforma do telhado da creche São Judas",
        organizer = "Associação de Moradores de Felipe Camarão",
        goalInCents = 1_800_000L,
        raisedInCents = 240_000L
    ),
    Campaign(
        id = "3",
        title = "Material escolar para 120 crianças",
        organizer = "ONG Semear",
        goalInCents = 350_000L,
        raisedInCents = 350_000L
    )
)