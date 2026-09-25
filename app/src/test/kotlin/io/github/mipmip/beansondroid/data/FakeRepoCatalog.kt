package io.github.mipmip.beansondroid.data

import io.github.mipmip.beansondroid.store.RepoCatalog
import io.github.mipmip.beansondroid.store.RepoConfig
import io.github.mipmip.beansondroid.store.RepoList
import io.github.mipmip.beansondroid.store.repoIdFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRepoCatalog : RepoCatalog {

    private val state = MutableStateFlow(RepoList())
    private val tokens = mutableMapOf<String, String>()

    override val repos: Flow<RepoList> = state

    override suspend fun current(): RepoList = state.value

    override suspend fun add(url: String, label: String, token: String?): RepoConfig {
        val config = RepoConfig(repoIdFor(url), url, label.ifBlank { "repo" }, !token.isNullOrBlank())
        token?.let { tokens[config.id] = it }
        state.value = state.value.add(config)
        return config
    }

    override suspend fun remove(id: String) {
        tokens.remove(id)
        state.value = state.value.remove(id)
    }

    override suspend fun activate(id: String) {
        state.value = state.value.activate(id)
    }

    override suspend fun tokenFor(id: String): String? = tokens[id]
}
