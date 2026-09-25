package io.github.mipmip.beansondroid.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.data.IndexState
import io.github.mipmip.beansondroid.index.BeanRelations
import io.github.mipmip.beansondroid.ui.BeanVisuals
import io.github.mipmip.beansondroid.ui.MarkdownText
import io.github.mipmip.beansondroid.ui.Message
import io.github.mipmip.beansondroid.ui.Pill
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIMESTAMP: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeanDetailScreen(
    viewModel: AppViewModel,
    beanId: String,
    onOpenBean: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.indexState.collectAsStateWithLifecycle()
    val ready = state as? IndexState.Ready
    val bean = ready?.beans?.index?.byId(beanId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = bean?.title?.ifEmpty { beanId } ?: beanId,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (bean == null) {
            Message(
                title = "That bean is not here",
                body = "Bean $beanId is not in this repository. It may have been renamed, " +
                    "archived or removed since this copy was fetched.",
                modifier = Modifier.padding(padding),
                actionLabel = "Back",
                onAction = onBack,
            )
            return@Scaffold
        }

        val relations = ready.beans.index.relations(beanId)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header(bean)
            HorizontalDivider()
            Frontmatter(bean)
            if (bean.extras.isNotEmpty()) {
                HorizontalDivider()
                Extras(bean)
            }
            HorizontalDivider()
            Relations(relations, onOpenBean)
            if (bean.body.isNotBlank()) {
                HorizontalDivider()
                MarkdownText(bean.body, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(bean: Bean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = bean.title.ifEmpty { "(untitled)" },
            style = MaterialTheme.typography.headlineSmall,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = bean.id,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (bean.status.isNotEmpty()) {
                Pill(bean.status, BeanVisuals.statusColor(bean.status, MaterialTheme.colorScheme))
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

@Composable
private fun Frontmatter(bean: Bean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Field("Priority", bean.priority)
        Field("Tags", bean.tags.joinToString(", "))
        Field("Order", bean.order)
        Field("Created", bean.createdAt?.let(::formatTime).orEmpty())
        Field("Updated", bean.updatedAt?.let(::formatTime).orEmpty())
        Field("Slug", bean.slug)
    }
}

@Composable
private fun Extras(bean: Bean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Other fields",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { contentDescription = "Other fields" },
        )
        bean.extras.forEach { (key, value) -> Field(key, value) }
    }
}

@Composable
private fun Field(label: String, value: String) {
    if (value.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.3f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Relations(relations: BeanRelations, onOpenBean: (String) -> Unit) {
    val hasAny = relations.parent != null ||
        relations.children.isNotEmpty() ||
        relations.blocking.isNotEmpty() ||
        relations.blockedBy.isNotEmpty() ||
        relations.unresolved.isNotEmpty()
    if (!hasAny) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Related beans",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { contentDescription = "Related beans" },
        )
        relations.parent?.let { Link("Parent", it, onOpenBean) }
        relations.children.forEach { Link("Child", it, onOpenBean) }
        relations.blocking.forEach { Link("Blocks", it, onOpenBean) }
        relations.blockedBy.forEach { Link("Blocked by", it, onOpenBean) }
        relations.unresolved.forEach { Unresolved(it) }
    }
}

@Composable
private fun Link(role: String, bean: Bean, onOpenBean: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenBean(bean.id) }
            .semantics { contentDescription = "$role ${bean.id}" }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.3f),
        )
        Text(
            text = bean.title.ifEmpty { bean.id },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Composable
private fun Unresolved(id: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Missing",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.3f),
        )
        Text(
            text = "$id is not in this repository",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatTime(instant: Instant): String = TIMESTAMP.format(instant)
