package io.github.mipmip.beansondroid.store

import kotlinx.serialization.Serializable

@Serializable
data class RepoConfig(
    val id: String,
    val url: String,
    val label: String,
    val hasToken: Boolean = false,
)

@Serializable
data class RepoList(
    val repos: List<RepoConfig> = emptyList(),
    val activeId: String? = null,
) {
    val active: RepoConfig? get() = repos.firstOrNull { it.id == activeId }

    fun add(repo: RepoConfig): RepoList {
        val existing = repos.firstOrNull { it.url == repo.url }
        if (existing != null) {
            return copy(
                repos = repos.map { if (it.id == existing.id) it.copy(label = repo.label, hasToken = repo.hasToken) else it },
                activeId = activeId ?: existing.id,
            )
        }
        return copy(repos = repos + repo, activeId = activeId ?: repo.id)
    }

    fun remove(id: String): RepoList {
        val remaining = repos.filterNot { it.id == id }
        val nextActive = when {
            activeId != id -> activeId
            else -> remaining.firstOrNull()?.id
        }
        return RepoList(remaining, nextActive)
    }

    fun activate(id: String): RepoList =
        if (repos.any { it.id == id }) copy(activeId = id) else this

    fun withToken(id: String, hasToken: Boolean): RepoList =
        copy(repos = repos.map { if (it.id == id) it.copy(hasToken = hasToken) else it })
}

fun repoIdFor(url: String): String =
    url.trim().lowercase().fold(0L) { acc, c -> acc * 31 + c.code }
        .toULong()
        .toString(36)
        .padStart(8, '0')
