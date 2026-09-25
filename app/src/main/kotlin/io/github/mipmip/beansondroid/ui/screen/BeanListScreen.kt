package io.github.mipmip.beansondroid.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.bean.ParseResult
import io.github.mipmip.beansondroid.data.Activity
import io.github.mipmip.beansondroid.data.IndexState
import io.github.mipmip.beansondroid.index.BeanFacets
import io.github.mipmip.beansondroid.index.BeanQuery
import io.github.mipmip.beansondroid.ui.BeanVisuals
import io.github.mipmip.beansondroid.ui.Message
import io.github.mipmip.beansondroid.ui.Pill
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeanListScreen(
    viewModel: AppViewModel,
    onOpenBean: (String) -> Unit,
    onOpenRepos: () -> Unit,
) {
    val state by viewModel.indexState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    var filtersOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state as? IndexState.Ready)?.repo?.label ?: "Beans on Droid",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { filtersOpen = !filtersOpen },
                        modifier = Modifier.semantics { contentDescription = "Filters" },
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = null)
                    }
                    IconButton(
                        onClick = onOpenRepos,
                        modifier = Modifier.semantics { contentDescription = "Repositories" },
                    ) {
                        Icon(Icons.Filled.Storage, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (val current = state) {
                IndexState.NoRepository -> Message(
                    title = "No repository yet",
                    body = "Add a beans repository to start reading its issues.",
                    actionLabel = "Add a repository",
                    onAction = onOpenRepos,
                )

                is IndexState.Working -> Working(current.what)

                is IndexState.Failed -> Message(
                    title = "Could not open ${current.repo?.label ?: "the repository"}",
                    body = AppViewModel.describe(current.error),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )

                is IndexState.Ready -> Ready(
                    beans = current.beans.index.query(query),
                    total = current.beans.index.size,
                    skipped = current.beans.skipped,
                    staleReason = current.staleReason,
                    facets = current.beans.index.facets,
                    query = query,
                    filtersOpen = filtersOpen,
                    viewModel = viewModel,
                    onOpenBean = onOpenBean,
                    onOpenRepos = onOpenRepos,
                )
            }
        }
    }
}

@Composable
private fun Working(what: Activity) {
    Message(
        title = when (what) {
            Activity.Cloning -> "Cloning"
            Activity.Refreshing -> "Refreshing"
            Activity.Indexing -> "Reading beans"
        },
        body = when (what) {
            Activity.Cloning -> "Fetching the repository for the first time."
            Activity.Refreshing -> "Fetching the latest commit."
            Activity.Indexing -> "Parsing the bean files."
        },
    )
}

@Composable
private fun Ready(
    beans: List<Bean>,
    total: Int,
    skipped: List<ParseResult.Skipped>,
    staleReason: String?,
    facets: BeanFacets,
    query: BeanQuery,
    filtersOpen: Boolean,
    viewModel: AppViewModel,
    onOpenBean: (String) -> Unit,
    onOpenRepos: () -> Unit,
) {
    var skippedOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (staleReason != null) {
            StaleBanner(staleReason, viewModel::refresh)
        }

        OutlinedTextField(
            value = query.term,
            onValueChange = viewModel::setTerm,
            singleLine = true,
            label = { Text("Search") },
            trailingIcon = {
                if (query.term.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.setTerm("") },
                        modifier = Modifier.semantics { contentDescription = "Clear search" },
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { contentDescription = "Search" },
        )

        if (filtersOpen) {
            Filters(facets, query, viewModel)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${beans.size} of $total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (skipped.isNotEmpty()) {
                Text(
                    text = "${skipped.size} file${if (skipped.size == 1) "" else "s"} " +
                        "could not be read",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { skippedOpen = true }
                        .semantics { contentDescription = "Files that could not be read" },
                )
            }
        }

        when {
            total == 0 -> Message(
                title = "This repository has no beans",
                body = "Its bean directory is empty. Pull down to refresh, or switch " +
                    "to another repository.",
                actionLabel = "Repositories",
                onAction = onOpenRepos,
            )

            beans.isEmpty() -> Message(
                title = "Nothing matches",
                body = "No bean matches the search and filters you have set.",
                actionLabel = "Clear filters",
                onAction = viewModel::clearFilters,
            )

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(beans, key = { it.id }) { bean ->
                    BeanRow(bean = bean, onClick = { onOpenBean(bean.id) })
                    HorizontalDivider()
                }
            }
        }
    }

    if (skippedOpen) {
        SkippedDialog(skipped) { skippedOpen = false }
    }
}

@Composable
private fun StaleBanner(reason: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = "Refresh failed" },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Showing the last fetched copy. Refresh failed: $reason",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun SkippedDialog(skipped: List<ParseResult.Skipped>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Files that could not be read") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "These files are in the bean directory but their frontmatter " +
                        "could not be parsed. Everything else was read normally.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                skipped.forEach { entry ->
                    Column {
                        Text(entry.fileName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = entry.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun Filters(facets: BeanFacets, query: BeanQuery, viewModel: AppViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FacetRow("Status", facets.statuses, query.statuses, viewModel::toggleStatus)
        FacetRow("Type", facets.types, query.types, viewModel::toggleType)
        if (facets.tags.isNotEmpty()) {
            FacetRow("Tag", facets.tags, query.tags, viewModel::toggleTag)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = query.includeArchived,
                onClick = viewModel::toggleArchived,
                label = { Text("Archived") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                modifier = Modifier.semantics { contentDescription = "Archived" },
            )
            FilterChip(
                selected = false,
                onClick = viewModel::clearFilters,
                label = { Text("Clear") },
                modifier = Modifier.semantics { contentDescription = "Clear filters" },
            )
        }
    }
}

@Composable
private fun FacetRow(
    title: String,
    values: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (values.isEmpty()) return
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value in selected,
                    onClick = { onToggle(value) },
                    label = { Text(value) },
                    modifier = Modifier.semantics { contentDescription = "$title $value" },
                )
            }
        }
    }
}

@Composable
private fun BeanRow(bean: Bean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = bean.title.ifEmpty { "(untitled)" },
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = bean.id,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (bean.status.isNotEmpty()) {
                Pill(
                    bean.status,
                    BeanVisuals.statusColor(bean.status, MaterialTheme.colorScheme),
                )
            }
            if (bean.type.isNotEmpty()) {
                Pill(bean.type, BeanVisuals.typeColor(bean.type, MaterialTheme.colorScheme))
            }
            if (bean.archived) {
                Pill("archived", MaterialTheme.colorScheme.outline)
            }
        }
    }
}
