package io.solidar.donation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform