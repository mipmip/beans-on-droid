package io.github.mipmip.beansondroid.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mipmip.beansondroid.store.RepoConfig
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val repos by viewModel.repos.collectAsStateWithLifecycle()
    val form by viewModel.addRepo.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<RepoConfig?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repositories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetAddRepo()
                    adding = true
                },
                modifier = Modifier.semantics { contentDescription = "Add repository" },
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
    ) { padding ->
        if (repos.repos.isEmpty()) {
            EmptyRepos(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(repos.repos, key = { it.id }) { repo ->
                    RepoRow(
                        repo = repo,
                        active = repo.id == repos.activeId,
                        onActivate = { viewModel.activate(repo.id) },
                        onRemove = { pendingRemoval = repo },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (adding) {
        AddRepoDialog(
            url = form.url,
            label = form.label,
            token = form.token,
            busy = form.busy,
            error = form.error,
            onUrl = viewModel::onAddRepoUrlChanged,
            onLabel = viewModel::onAddRepoLabelChanged,
            onToken = viewModel::onAddRepoTokenChanged,
            onDismiss = {
                if (!form.busy) {
                    viewModel.resetAddRepo()
                    adding = false
                }
            },
            onConfirm = { viewModel.addRepository { adding = false } },
        )
    }

    pendingRemoval?.let { repo ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text("Remove ${repo.label}?") },
            text = {
                Text(
                    "The local copy and any token for this repository are deleted from this " +
                        "device. Nothing on the server changes.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRepository(repo.id)
                    pendingRemoval = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun RepoRow(
    repo: RepoConfig,
    active: Boolean,
    onActivate: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onActivate)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (active) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = if (active) "Active repository" else "Inactive repository",
            tint = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(repo.label, style = MaterialTheme.typography.titleMedium)
                if (repo.hasToken) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Uses a token",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                text = repo.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove ${repo.label}")
        }
    }
}

@Composable
private fun EmptyRepos(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No repositories yet", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Add one with its https:// clone URL. For a private repository, " +
                    "supply a personal access token as well.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddRepoDialog(
    url: String,
    label: String,
    token: String,
    busy: Boolean,
    error: String?,
    onUrl: (String) -> Unit,
    onLabel: (String) -> Unit,
    onToken: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrl,
                    enabled = !busy,
                    singleLine = true,
                    label = { Text("Clone URL") },
                    placeholder = { Text("https://github.com/hmans/beans.git") },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Clone URL" },
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = onLabel,
                    enabled = !busy,
                    singleLine = true,
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Name" },
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = onToken,
                    enabled = !busy,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Access token (private repositories)") },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Access token" },
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (busy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Text("Cloning, this can take a moment")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}
