package io.solidar.donation.domain.repository

import io.solidar.donation.domain.model.AlterarSenhaInput
import io.solidar.donation.domain.model.AtualizarPerfilInput
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.domain.model.ResultadoCacheado

interface PerfilRepository {
    suspend fun carregarPerfil(): Result<ResultadoCacheado<PerfilUsuario>>

    suspend fun atualizarPerfil(input: AtualizarPerfilInput): Result<PerfilUsuario>

    suspend fun alterarSenha(input: AlterarSenhaInput): Result<Unit>
}
