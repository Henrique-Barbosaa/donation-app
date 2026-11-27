package io.solidar.donation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import io.solidar.donation.data.remote.ApiConfig

/** O backend devolve caminhos relativos (`/uploads/...`); aqui montamos a URL completa. */
fun resolverUrlImagem(caminho: String?): String? =
    when {
        caminho.isNullOrBlank() -> null
        caminho.startsWith("http://") || caminho.startsWith("https://") -> caminho
        else -> ApiConfig.baseUrl.trimEnd('/') + "/" + caminho.trimStart('/')
    }

/** Mostra a imagem de [caminho]; sem imagem ou com falha, exibe um quadro neutro com ícone. */
@Composable
fun ImagemRemota(
    caminho: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val url = resolverUrlImagem(caminho)
    if (url == null) {
        ImagemIndisponivel(modifier)
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            },
            error = { ImagemIndisponivel(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun ImagemIndisponivel(modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Camera,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp),
        )
    }
}
