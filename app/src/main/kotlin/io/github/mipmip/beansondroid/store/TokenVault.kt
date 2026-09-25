package io.github.mipmip.beansondroid.store

interface TokenVault {
    suspend fun put(id: String, token: String)

    suspend fun get(id: String): String?

    suspend fun remove(id: String)
}

class InMemoryTokenVault : TokenVault {
    private val tokens = mutableMapOf<String, String>()

    override suspend fun put(id: String, token: String) {
        tokens[id] = token
    }

    override suspend fun get(id: String): String? = tokens[id]

    override suspend fun remove(id: String) {
        tokens.remove(id)
    }
}
