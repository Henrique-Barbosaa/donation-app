package io.solidar.donation.util

import androidx.compose.runtime.mutableStateListOf

object FavoritosManager {
    val favoritosIds = mutableStateListOf<String>()

    fun toggle(id: String) {
        if (favoritosIds.contains(id)) {
            favoritosIds.remove(id)
        } else {
            favoritosIds.add(id)
        }
    }

    fun isFavorito(id: String): Boolean {
        return favoritosIds.contains(id)
    }
}
