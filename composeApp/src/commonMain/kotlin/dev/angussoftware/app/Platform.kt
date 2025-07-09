package dev.angussoftware.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform