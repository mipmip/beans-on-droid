package io.github.mipmip.beansondroid.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.TestCatalog
import io.github.mipmip.beansondroid.TestRepo
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.ui.screen.BeanDetailScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import io.noties.markwon.ext.tables.TableRowSpan
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class BeanDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var beans: BeansRepository
    private val opened = mutableListOf<String>()
    private var backs = 0

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "bean-detail-${System.nanoTime()}").apply { mkdirs() }
        val catalog = TestCatalog()
        beans = TestRepo.beansRepository(scratch, catalog)
        opened.clear()
        backs = 0

        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(
                Triple("x-0001", "Parent epic", TestRepo.bean("Parent epic", type = "epic")),
                Triple(
                    "x-0002",
                    "Child task",
                    buildString {
                        appendLine("---")
                        appendLine("title: Child task")
                        appendLine("status: in-progress")
                        appendLine("type: task")
                        appendLine("priority: high")
                        appendLine("tags:")
                        appendLine("    - core")
                        appendLine("created_at: 2026-01-02T03:04:05Z")
                        appendLine("parent: x-0001")
                        appendLine("blocked_by:")
                        appendLine("    - x-0003")
                        appendLine("    - x-ghost")
                        appendLine("assignee: someone")
                        appendLine("---")
                        appendLine()
                        appendLine("## A heading")
                        appendLine()
                        appendLine("Some **bold** body text.")
                        appendLine()
                        appendLine("The parent is x-0001 and the retired one is x-0004.")
                        appendLine()
                        appendLine("A word that looks like an id is not-a-real-bean here.")
                        appendLine()
                        appendLine("The raw file sits at .beans/x-0001--parent-epic.md.")
                        appendLine()
                        appendLine("| Field | Value |")
                        appendLine("| --- | --- |")
                        appendLine("| Parent | x-0001 |")
                        appendLine()
                        appendLine("Run `./scripts/gate.sh` before shipping.")
                        appendLine("A code mention is `x-0003` here.")
                    },
                ),
                Triple("x-0003", "Blocking task", TestRepo.bean("Blocking task")),
                Triple("x-0004", "Retired task", TestRepo.bean("Retired task", status = "completed")),
            ),
        )
        runBlocking {
            val config = catalog.add(url, "Demo", null)
            catalog.activate(config.id)
            beans.loadActive()
        }
    }

    private fun show(beanId: String) {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeanDetailScreen(
                    viewModel = model,
                    beanId = beanId,
                    onOpenBean = { opened += it },
                    onBack = { backs++ },
                )
            }
        }
    }

    private fun clipboard(): ClipboardManager {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private fun clipboardText(): String =
        clipboard().primaryClip?.getItemAt(0)?.text?.toString().orEmpty()

    @Test
    fun theIdIsOfferedAsACopyableChip() {
        show("x-0002")
        compose.onNodeWithContentDescription("Copy id x-0002").assertIsDisplayed()
        compose.onNodeWithText("x-0002").assertIsDisplayed()
    }

    @Test
    fun copyingTheIdPutsTheBareIdOnTheClipboard() {
        show("x-0002")
        compose.runOnUiThread {
            clipboard().setPrimaryClip(ClipData.newPlainText("other", "something else"))
        }
        compose.onNodeWithContentDescription("Copy id x-0002").performClick()
        compose.waitUntil(5_000) { clipboardText() == "x-0002" }
        assertEquals("x-0002", clipboardText())
    }

    @Test
    fun theCopyIsConfirmed() {
        show("x-0002")
        compose.onNodeWithContentDescription("Copy id x-0002").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("Copied x-0002") }
        compose.onNodeWithText("Copied x-0002").assertIsDisplayed()
    }

    @Test
    fun noCopyAffordanceForAnUnknownBean() {
        show("x-nope")
        compose.onAllNodesWithContentDescription("Copy id x-nope").assertCountEquals(0)
    }

    @Test
    fun frontmatterFieldsAreShown() {
        show("x-0002")
        compose.onAllNodesWithText("Child task").assertCountEquals(2)
        compose.onNodeWithText("x-0002").assertIsDisplayed()
        compose.onNodeWithText("high").assertIsDisplayed()
        compose.onNodeWithText("core").assertIsDisplayed()
        compose.onNodeWithText("in-progress").assertIsDisplayed()
        compose.onNodeWithText("2 Jan 2026", substring = true).assertIsDisplayed()
    }

    @Test
    fun unrecognisedFieldsAreShownRaw() {
        show("x-0002")
        compose.onNodeWithContentDescription("Other fields").assertIsDisplayed()
        compose.onNodeWithText("assignee").assertIsDisplayed()
        compose.onNodeWithText("someone").assertIsDisplayed()
    }

    @Test
    fun theMarkdownBodyIsRendered() {
        show("x-0002")
        compose.waitForIdle()
        onView(withSubstring("A heading")).check(matches(isDisplayed()))
        onView(withSubstring("Some bold body text.")).check(matches(isDisplayed()))
    }

    @Test
    fun relationshipsAreListedAndTappable() {
        show("x-0002")
        compose.onNodeWithContentDescription("Related beans").assertIsDisplayed()
        compose.onNodeWithContentDescription("Parent x-0001").assertIsDisplayed()
        compose.onNodeWithContentDescription("Blocked by x-0003").performClick()
        assertEquals(listOf("x-0003"), opened)
    }

    @Test
    fun childrenAreListedOnTheParent() {
        show("x-0001")
        compose.onNodeWithContentDescription("Child x-0002").assertIsDisplayed()
    }

    @Test
    fun aMissingRelatedBeanIsShownWithoutALink() {
        show("x-0002")
        compose.onNodeWithText("x-ghost is not in this repository").assertIsDisplayed()
    }

    @Test
    fun anUnknownBeanDegradesGracefully() {
        show("x-nope")
        compose.onNodeWithText("That bean is not here").assertIsDisplayed()
        compose.onNodeWithText("Back").performClick()
        assertTrue(backs > 0)
    }

    private fun bodyView(marker: String): TextView {
        lateinit var found: TextView
        onView(allOf(withSubstring(marker), isAssignableFrom(TextView::class.java)))
            .check { view, _ -> found = view as TextView }
        return found
    }

    private fun mentionSpans(view: TextView, id: String): List<ClickableSpan> {
        val text = view.text as Spanned
        val start = text.indexOf(id)
        return text.getSpans(start, start + id.length, ClickableSpan::class.java).toList()
    }

    private fun paintOf(span: ClickableSpan): TextPaint =
        TextPaint().also { span.updateDrawState(it) }

    private fun closeInRemote(id: String) {
        val dir = File(scratch, "remote")
        val file = File(dir, ".beans").listFiles().orEmpty().first { it.name.startsWith("$id--") }
        file.writeText(file.readText().replace("status: todo", "status: completed"))
        Git.open(dir).use { git ->
            git.add().addFilepattern(".").call()
            git.commit().setMessage("close $id").setSign(false).call()
        }
    }

    @Test
    fun aMentionInTheBodyIsClickableAndOpensTheBean() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        val text = view.text as Spanned
        val span = mentionSpans(view, "x-0001").single()
        assertEquals(text.indexOf("x-0001"), text.getSpanStart(span))
        assertEquals(text.indexOf("x-0001") + "x-0001".length, text.getSpanEnd(span))
        compose.runOnUiThread { span.onClick(view) }
        assertEquals(listOf("x-0001"), opened)
    }

    @Test
    fun aClosedMentionIsStruckThroughAndAnOpenOneIsNot() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        val open = paintOf(mentionSpans(view, "x-0001").single())
        val closed = paintOf(mentionSpans(view, "x-0004").single())
        assertFalse(open.isStrikeThruText)
        assertTrue(closed.isStrikeThruText)
        assertTrue(open.bgColor != 0)
        assertEquals(0, closed.bgColor)
    }

    @Test
    fun anIdShapedWordNoBeanHasStaysOrdinary() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        assertTrue(mentionSpans(view, "not-a-real-bean").isEmpty())
    }

    @Test
    fun aMentionIsStruckThroughAfterTheRepositoryIsReadAgain() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        assertFalse(paintOf(mentionSpans(view, "x-0001").single()).isStrikeThruText)

        closeInRemote("x-0001")
        runBlocking { beans.refresh() }
        compose.waitForIdle()

        val reread = bodyView("The parent is")
        assertTrue(paintOf(mentionSpans(reread, "x-0001").single()).isStrikeThruText)
    }

    @Test
    fun aCodeSpanAndATableReadAlongsideTheMentions() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        val text = view.text as Spanned
        assertTrue(text.toString(), text.contains("./scripts/gate.sh"))
        assertTrue(text.getSpans(0, text.length, TableRowSpan::class.java).isNotEmpty())
        assertTrue(mentionSpans(view, "x-0001").isNotEmpty())
        assertTrue(mentionSpans(view, "x-0003").isNotEmpty())
        onView(allOf(withSubstring("./scripts/gate.sh"), isAssignableFrom(TextView::class.java)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun aMentionInsideATableCellIsMarkedAndOpensTheBean() {
        show("x-0002")
        compose.waitForIdle()
        val view = bodyView("The parent is")
        val outer = view.text as Spanned
        val cell = outer.getSpans(0, outer.length, TableRowSpan::class.java)
            .flatMap { MarkwonTables.cells(it) }
            .first { it.text().contains("x-0001") }
        val content = cell.text() as Spanned
        val at = content.toString().indexOf("x-0001")
        val span = content.getSpans(at, at + "x-0001".length, ClickableSpan::class.java).single()
        assertEquals(at, content.getSpanStart(span))
        compose.runOnUiThread { span.onClick(view) }
        assertEquals(listOf("x-0001"), opened)
    }
}
