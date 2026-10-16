package io.github.mipmip.beansondroid.ui

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.bean.BeanVocabulary
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableRowSpan
import io.noties.markwon.ext.tasklist.TaskListPlugin

@Composable
fun MarkdownText(
    markdown: String,
    lookup: (String) -> Bean?,
    onOpenBean: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .build()
    }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    val mentions = MentionTheme(
        color = MaterialTheme.colorScheme.onPrimaryContainer.toArgb(),
        background = MaterialTheme.colorScheme.primaryContainer.toArgb(),
        closed = MaterialTheme.colorScheme.outline.toArgb(),
    )

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(it).apply {
                movementMethod = TableAwareMovementMethod.create()
            }
        },
        update = { view ->
            view.setTextColor(textColor)
            view.setLinkTextColor(linkColor)
            view.textSize = 15f
            val text = SpannableStringBuilder(markwon.toMarkdown(markdown))
            mentions.mark(text, lookup, onOpenBean)
            for (row in text.getSpans(0, text.length, TableRowSpan::class.java)) {
                for (cell in MarkwonTables.cells(row)) {
                    (cell.text() as? Spannable)?.let { mentions.mark(it, lookup, onOpenBean) }
                }
            }
            markwon.setParsedMarkdown(view, text)
        },
    )
}

private class MentionTheme(
    private val color: Int,
    private val background: Int,
    private val closed: Int,
) {

    fun mark(text: Spannable, lookup: (String) -> Bean?, onOpenBean: (String) -> Unit) {
        for (mention in BeanMentions.find(text.toString(), lookup)) {
            if (text.getSpans(mention.start, mention.end, URLSpan::class.java).isNotEmpty()) {
                continue
            }
            val isClosed = BeanVocabulary.isClosed(mention.bean.status)
            text.setSpan(
                MentionSpan(
                    color = if (isClosed) closed else color,
                    background = if (isClosed) 0 else background,
                    strikethrough = isClosed,
                    onOpen = { onOpenBean(mention.bean.id) },
                ),
                mention.start,
                mention.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            text.setSpan(
                TypefaceSpan("monospace"),
                mention.start,
                mention.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }
}

private class MentionSpan(
    private val color: Int,
    private val background: Int,
    private val strikethrough: Boolean,
    private val onOpen: () -> Unit,
) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        ds.color = color
        ds.bgColor = background
        ds.isUnderlineText = false
        ds.isStrikeThruText = strikethrough
    }

    override fun onClick(widget: View) {
        onOpen()
    }
}
