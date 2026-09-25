package io.github.mipmip.beansondroid

import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.store.InMemoryTokenVault
import io.github.mipmip.beansondroid.store.RepoCatalog
import io.github.mipmip.beansondroid.store.RepoConfig
import io.github.mipmip.beansondroid.store.RepoList
import io.github.mipmip.beansondroid.store.repoIdFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.eclipse.jgit.api.Git
import java.io.File

class TestCatalog : RepoCatalog {
    private val state = MutableStateFlow(RepoList())
    private val vault = InMemoryTokenVault()

    override val repos: Flow<RepoList> = state

    override suspend fun current(): RepoList = state.value

    override suspend fun add(url: String, label: String, token: String?): RepoConfig {
        val config = RepoConfig(repoIdFor(url), url, label.ifBlank { "repo" }, !token.isNullOrBlank())
        token?.let { vault.put(config.id, it) }
        state.value = state.value.add(config)
        return config
    }

    override suspend fun remove(id: String) {
        vault.remove(id)
        state.value = state.value.remove(id)
    }

    override suspend fun activate(id: String) {
        state.value = state.value.activate(id)
    }

    override suspend fun tokenFor(id: String): String? = vault.get(id)
}

object TestRepo {

    fun beansRepository(root: File, catalog: RepoCatalog): BeansRepository =
        BeansRepository(catalog, RepoStore(File(root, "repos")))

    fun remote(root: File, name: String, beans: List<Triple<String, String, String>>): String {
        val dir = File(root, name).apply { mkdirs() }
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, ".beans").mkdirs()
            File(dir, ".beans/.gitkeep").writeText("")
            File(dir, ".beans.yml").writeText("beans:\n    path: .beans\n")
            beans.forEach { (id, title, body) ->
                File(dir, ".beans/$id--${title.lowercase().replace(' ', '-')}.md").writeText(body)
            }
            git.add().addFilepattern(".").call()
            git.commit().setMessage("beans").setSign(false).call()
        }
        return dir.toURI().toString()
    }

    fun bean(
        title: String,
        status: String = "todo",
        type: String = "task",
        tags: List<String> = emptyList(),
        parent: String? = null,
        blockedBy: List<String> = emptyList(),
        body: String = "Body of $title.",
    ): String = buildString {
        appendLine("---")
        appendLine("title: $title")
        appendLine("status: $status")
        appendLine("type: $type")
        if (tags.isNotEmpty()) {
            appendLine("tags:")
            tags.forEach { appendLine("    - $it") }
        }
        parent?.let { appendLine("parent: $it") }
        if (blockedBy.isNotEmpty()) {
            appendLine("blocked_by:")
            blockedBy.forEach { appendLine("    - $it") }
        }
        appendLine("---")
        appendLine()
        appendLine(body)
    }
}
