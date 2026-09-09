package org.donation.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform