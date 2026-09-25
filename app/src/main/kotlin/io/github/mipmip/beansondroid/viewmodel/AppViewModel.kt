package io.github.mipmip.beansondroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.data.IndexState
import io.github.mipmip.beansondroid.index.BeanQuery
import io.github.mipmip.beansondroid.repo.RepoError
import io.github.mipmip.beansondroid.repo.RepoResult
import io.github.mipmip.beansondroid.store.RepoList
import io.github.mipmip.beansondroid.store.RepoUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddRepoState(
    val url: String = "",
    val label: String = "",
    val token: String = "",
    val busy: Boolean = false,
    val error: String? = null,
)

class AppViewModel(
    private val repository: BeansRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val validateUrl: (String) -> String? = RepoUrl::validate,
) : ViewModel() {

    val indexState: StateFlow<IndexState> = repository.indexState

    val repos: StateFlow<RepoList> = repository.repos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RepoList())

    private val _query = MutableStateFlow(BeanQuery())
    val query: StateFlow<BeanQuery> = _query.asStateFlow()

    private val _addRepo = MutableStateFlow(AddRepoState())
    val addRepo: StateFlow<AddRepoState> = _addRepo.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        viewModelScope.launch { withContext(io) { repository.loadActiveIfNeeded() } }
    }

    fun setTerm(term: String) {
        _query.value = _query.value.copy(term = term)
    }

    fun toggleStatus(status: String) = _query.update { it.copy(statuses = it.statuses.toggle(status)) }

    fun toggleType(type: String) = _query.update { it.copy(types = it.types.toggle(type)) }

    fun toggleTag(tag: String) = _query.update { it.copy(tags = it.tags.toggle(tag)) }

    fun toggleArchived() = _query.update { it.copy(includeArchived = !it.includeArchived) }

    fun clearFilters() {
        _query.value = BeanQuery(term = _query.value.term)
    }

    fun onAddRepoUrlChanged(url: String) = _addRepo.update { it.copy(url = url, error = null) }

    fun onAddRepoLabelChanged(label: String) = _addRepo.update { it.copy(label = label) }

    fun onAddRepoTokenChanged(token: String) = _addRepo.update { it.copy(token = token) }

    fun resetAddRepo() {
        _addRepo.value = AddRepoState()
    }

    fun addRepository(onAdded: () -> Unit = {}) {
        val form = _addRepo.value
        if (form.busy) return

        val invalid = validateUrl(form.url)
        if (invalid != null) {
            _addRepo.value = form.copy(error = invalid)
            return
        }

        _addRepo.value = form.copy(busy = true, error = null)
        viewModelScope.launch {
            val result = withContext(io) {
                repository.addRepository(form.url, form.label, form.token.takeIf { it.isNotBlank() })
            }
            when (result) {
                is RepoResult.Success -> {
                    _addRepo.value = AddRepoState()
                    onAdded()
                }

                is RepoResult.Failure -> _addRepo.update {
                    it.copy(busy = false, error = describe(result.error))
                }
            }
        }
    }

    fun activate(id: String) {
        viewModelScope.launch { withContext(io) { repository.activate(id) } }
    }

    fun removeRepository(id: String) {
        viewModelScope.launch { withContext(io) { repository.removeRepository(id) } }
    }

    fun refresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            withContext(io) { repository.refresh() }
            _refreshing.value = false
        }
    }

    fun retry() {
        viewModelScope.launch { withContext(io) { repository.loadActiveIfNeeded() } }
    }

    private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
        value = block(value)
    }

    companion object {
        fun describe(error: RepoError): String = when (error) {
            is RepoError.Authentication ->
                "Could not sign in to this repository. Check the personal access token."

            is RepoError.Network ->
                "Could not reach the repository. Check the URL and your connection."

            is RepoError.NotABeansRepository ->
                "This repository has no beans in it. ${error.message}."

            is RepoError.Unknown -> error.message
        }

        fun factory(repository: BeansRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(repository) as T
        }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value
