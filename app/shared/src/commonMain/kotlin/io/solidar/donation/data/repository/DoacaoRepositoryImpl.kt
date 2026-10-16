package io.solidar.donation.data.repository

import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.repository.DoacaoRepository

class DoacaoRepositoryImpl(
    private val tokenStorage: TokenStorage = SessionStorage.instance,
    private val remoteDataSource: DoacaoRemoteDataSource =
        DoacaoRemoteDataSource(
            httpClient = HttpClientFactory.create { tokenStorage.getToken() },
        ),
) : DoacaoRepository {
    override suspend fun criarDoacao(
        request: CriarDoacaoRequest,
        imagemBytes: ByteArray?,
        imagemNome: String?,
    ): Result<DoacaoResponse> {
        return runCatching {
            remoteDataSource.criarDoacao(
                request = request,
                imagemBytes = imagemBytes,
                imagemNome = imagemNome,
            )
        }
    }
}
