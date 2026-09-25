package io.github.mipmip.beansondroid.store

interface TokenVault {
    suspend fun put(id: String, token: String)

    suspend fun get(id: String): String?

    suspend fun remove(id: String)
}
