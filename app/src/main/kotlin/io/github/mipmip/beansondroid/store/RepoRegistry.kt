package io.github.mipmip.beansondroid.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.repoDataStore: DataStore<Preferences> by preferencesDataStore("repos")

private val REPOS = stringPreferencesKey("repos")

class RepoRegistry(
    private val dataStore: DataStore<Preferences>,
    private val vault: TokenVault,
) {

    constructor(context: Context, vault: TokenVault) :
        this(context.applicationContext.repoDataStore, vault)

    val repos: Flow<RepoList> = dataStore.data.map { prefs -> decode(prefs[REPOS]) }

    suspend fun current(): RepoList = repos.first()

    suspend fun add(url: String, label: String, token: String?): RepoConfig {
        val trimmed = url.trim()
        val id = repoIdFor(trimmed)
        if (!token.isNullOrBlank()) vault.put(id, token)
        val config = RepoConfig(
            id = id,
            url = trimmed,
            label = label.ifBlank { labelFor(trimmed) },
            hasToken = !token.isNullOrBlank(),
        )
        update { it.add(config) }
        return config
    }

    suspend fun remove(id: String) {
        vault.remove(id)
        update { it.remove(id) }
    }

    suspend fun activate(id: String) = update { it.activate(id) }

    suspend fun tokenFor(id: String): String? = vault.get(id)

    private suspend fun update(transform: (RepoList) -> RepoList) {
        dataStore.edit { prefs ->
            prefs[REPOS] = JSON.encodeToString(transform(decode(prefs[REPOS])))
        }
    }

    private fun decode(raw: String?): RepoList = when {
        raw.isNullOrBlank() -> RepoList()
        else -> runCatching { JSON.decodeFromString<RepoList>(raw) }.getOrDefault(RepoList())
    }

    private companion object {
        val JSON = Json { ignoreUnknownKeys = true }
    }
}

fun labelFor(url: String): String = url
    .trim()
    .removeSuffix("/")
    .removeSuffix(".git")
    .substringAfterLast('/')
    .ifBlank { url }
