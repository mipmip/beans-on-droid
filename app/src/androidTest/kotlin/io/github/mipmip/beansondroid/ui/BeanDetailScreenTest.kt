package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
                    },
                ),
                Triple("x-0003", "Blocking task", TestRepo.bean("Blocking task")),
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
}
