package io.github.grokipedia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform