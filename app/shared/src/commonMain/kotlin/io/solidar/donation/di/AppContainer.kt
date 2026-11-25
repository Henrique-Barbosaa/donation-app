package io.solidar.donation.di

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.AuthRemoteDataSource
import io.solidar.donation.data.remote.CampanhaRemoteDataSource
import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.repository.AuthRepositoryImpl
import io.solidar.donation.data.repository.CampanhaRepositoryImpl
import io.solidar.donation.data.repository.DoacaoRepositoryImpl
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.repository.AuthRepository
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.ui.auth.AuthViewModel
import io.solidar.donation.ui.campaign.CriarCampanhaViewModel
import io.solidar.donation.ui.donation.CriarDoacaoViewModel
import io.solidar.donation.ui.donation.DoarDinheiroViewModel
import io.solidar.donation.ui.donation.DoarItensCampanhaViewModel
import io.solidar.donation.ui.donation.EfetivarDoacaoViewModel
import io.solidar.donation.ui.explore.ExplorarViewModel
import io.solidar.donation.ui.history.HistoricoViewModel
import io.solidar.donation.ui.home.CampanhasViewModel

/**
 * Container central de injeção de dependências da aplicação.
 *
 * Responsável por montar as implementações da camada `data` e injetar as interfaces da camada
 * `domain` nos construtores dos ViewModels da camada `ui`, sem que os ViewModels instanciem
 * repositórios diretamente.
 */
class AppContainer(
    val tokenStorage: TokenStorage = SessionStorage.instance,
    val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
    httpClient: io.ktor.client.HttpClient = HttpClientFactory.create { tokenStorage.getToken() },
    val authRepository: AuthRepository =
        AuthRepositoryImpl(
            tokenStorage = tokenStorage,
            remoteDataSource = AuthRemoteDataSource(httpClient = httpClient),
        ),
    val campanhaRepository: CampanhaRepository =
        CampanhaRepositoryImpl(
            remoteDataSource = CampanhaRemoteDataSource(httpClient = httpClient),
            localCacheStorage = localCacheStorage,
            tokenStorage = tokenStorage,
        ),
    val doacaoRepository: DoacaoRepository =
        DoacaoRepositoryImpl(
            tokenStorage = tokenStorage,
            remoteDataSource = DoacaoRemoteDataSource(httpClient = httpClient),
            localCacheStorage = localCacheStorage,
        ),
) {
    fun createAuthViewModel(): AuthViewModel = AuthViewModel(authRepository)

    fun createCampanhasViewModel(): CampanhasViewModel = CampanhasViewModel(campanhaRepository)

    fun createHistoricoViewModel(): HistoricoViewModel = HistoricoViewModel(doacaoRepository, campanhaRepository)

    fun createExplorarViewModel(): ExplorarViewModel = ExplorarViewModel(doacaoRepository)

    fun createCriarDoacaoViewModel(): CriarDoacaoViewModel = CriarDoacaoViewModel(doacaoRepository)

    fun createCriarCampanhaViewModel(): CriarCampanhaViewModel = CriarCampanhaViewModel(campanhaRepository)

    fun createEfetivarDoacaoViewModel(): EfetivarDoacaoViewModel = EfetivarDoacaoViewModel(doacaoRepository)

    fun createDoarItensCampanhaViewModel(): DoarItensCampanhaViewModel = DoarItensCampanhaViewModel(doacaoRepository)

    fun createDoarDinheiroViewModel(): DoarDinheiroViewModel = DoarDinheiroViewModel(doacaoRepository)

    companion object {
        val default: AppContainer by lazy { AppContainer() }
    }
}
