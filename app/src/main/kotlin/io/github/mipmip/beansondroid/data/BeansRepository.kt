package io.github.mipmip.beansondroid.data

import io.github.mipmip.beansondroid.repo.RepoError
import io.github.mipmip.beansondroid.repo.RepoResult
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.store.RepoCatalog
import io.github.mipmip.beansondroid.store.RepoConfig
import io.github.mipmip.beansondroid.store.RepoList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface IndexState {
    data object NoRepository : IndexState

    data class Working(val what: Activity, val repo: RepoConfig?) : IndexState

    data class Ready(val repo: RepoConfig, val beans: LoadedBeans) : IndexState

    data class Failed(val repo: RepoConfig?, val error: RepoError) : IndexState
}

enum class Activity { Cloning, Refreshing, Indexing }

class BeansRepository(
    private val catalog: RepoCatalog,
    private val store: RepoStore,
    private val loader: BeanLoader = BeanLoader(),
) {

    val repos: Flow<RepoList> = catalog.repos

    private val _indexState = MutableStateFlow<IndexState>(IndexState.NoRepository)
    val indexState: StateFlow<IndexState> = _indexState.asStateFlow()

    suspend fun addRepository(url: String, label: String, token: String?): RepoResult<RepoConfig> {
        val config = catalog.add(url.trim(), label, token)
        _indexState.value = IndexState.Working(Activity.Cloning, config)

        return when (val cloned = store.clone(config.id, config.url, token)) {
            is RepoResult.Failure -> {
                catalog.remove(config.id)
                _indexState.value = IndexState.Failed(config, cloned.error)
                cloned
            }

            is RepoResult.Success -> {
                catalog.activate(config.id)
                indexActive(config)
                RepoResult.Success(config)
            }
        }
    }

    suspend fun activate(id: String) {
        catalog.activate(id)
        loadActive()
    }

    suspend fun removeRepository(id: String) {
        store.delete(id)
        catalog.remove(id)
        loadActive()
    }

    suspend fun loadActive() {
        val active = catalog.current().active
        if (active == null) {
            _indexState.value = IndexState.NoRepository
            return
        }
        if (!store.isCloned(active.id)) {
            _indexState.value = IndexState.Working(Activity.Cloning, active)
            val token = catalog.tokenFor(active.id)
            when (val cloned = store.clone(active.id, active.url, token)) {
                is RepoResult.Failure -> {
                    _indexState.value = IndexState.Failed(active, cloned.error)
                    return
                }

                is RepoResult.Success -> Unit
            }
        }
        indexActive(active)
    }

    suspend fun refresh() {
        val active = catalog.current().active ?: run {
            _indexState.value = IndexState.NoRepository
            return
        }
        _indexState.value = IndexState.Working(Activity.Refreshing, active)
        when (val refreshed = store.refresh(active.id, catalog.tokenFor(active.id))) {
            is RepoResult.Failure -> _indexState.value = IndexState.Failed(active, refreshed.error)
            is RepoResult.Success -> indexActive(active)
        }
    }

    private fun indexActive(repo: RepoConfig) {
        _indexState.value = IndexState.Working(Activity.Indexing, repo)
        when (val dir = store.beanDirectory(repo.id)) {
            is RepoResult.Failure -> _indexState.value = IndexState.Failed(repo, dir.error)
            is RepoResult.Success -> _indexState.value =
                IndexState.Ready(repo, loader.load(dir.value))
        }
    }
}
