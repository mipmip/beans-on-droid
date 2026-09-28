package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.TestCatalog
import io.github.mipmip.beansondroid.TestRepo
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.index.BeanSort
import io.github.mipmip.beansondroid.ui.screen.BeanListScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class SortingAndNestingTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var catalog: TestCatalog
    private lateinit var beans: BeansRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "sorting-${System.nanoTime()}").apply { mkdirs() }
        catalog = TestCatalog()
        beans = TestRepo.beansRepository(scratch, catalog)

        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(
                Triple("b-0001", "Zulu epic", TestRepo.bean("Zulu epic", status = "in-progress", type = "epic")),
                Triple("b-0002", "Alpha child", TestRepo.bean("Alpha child", status = "todo", type = "task", parent = "b-0001")),
                Triple("b-0003", "Bravo child", TestRepo.bean("Bravo child", status = "draft", type = "task", parent = "b-0001")),
                Triple("b-0004", "Mike loose", TestRepo.bean("Mike loose", status = "todo", type = "bug")),
            ),
        )
        runBlocking {
            val config = catalog.add(url, "Demo", null)
            catalog.activate(config.id)
            beans.loadActive()
        }
    }

    private fun show(): AppViewModel {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeanListScreen(viewModel = model, onOpenBean = {}, onOpenRepos = {})
            }
        }
        return model
    }

    @Test
    fun theDefaultListIsNestedAndStatusOrdered() {
        show()
        compose.onNodeWithText("4 of 4").assertIsDisplayed()
        compose.onNodeWithContentDescription("b-0001").assertIsDisplayed()
        compose.onNodeWithContentDescription("Nested b-0002").assertIsDisplayed()
        compose.onNodeWithContentDescription("Nested b-0003").assertIsDisplayed()
        compose.onNodeWithContentDescription("b-0004").assertIsDisplayed()
    }

    @Test
    fun choosingASortFlattensAndReorders() {
        val model = show()
        compose.onNodeWithContentDescription("Sort, Default order, Ascending").performClick()
        compose.onNodeWithContentDescription("Sort by Title").performClick()

        compose.waitUntil(5_000) { model.query.value.sort == BeanSort.Title }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("b-0002").assertIsDisplayed()
        compose.onNodeWithContentDescription("b-0003").assertIsDisplayed()
        compose.onAllNodes(
            androidx.compose.ui.test.hasContentDescription("Nested", substring = true),
        ).fetchSemanticsNodes().let { assertEquals(0, it.size) }
    }

    @Test
    fun reversingTheDirection() {
        val model = show()
        compose.onNodeWithContentDescription("Sort, Default order, Ascending").performClick()
        compose.onNodeWithContentDescription("Sort by Title").performClick()
        compose.waitUntil(5_000) { model.query.value.sort == BeanSort.Title }

        compose.onNodeWithContentDescription("Sort, Title, Ascending").performClick()
        compose.onNodeWithContentDescription("Reverse order").performClick()
        compose.waitUntil(5_000) {
            model.query.value.effectiveDirection ==
                io.github.mipmip.beansondroid.index.SortDirection.Descending
        }
        compose.onNodeWithContentDescription("Sort, Title, Descending").assertIsDisplayed()
    }

    @Test
    fun searchingFlattensAndClearingRestoresTheTree() {
        show()
        compose.onNodeWithContentDescription("Nested b-0002").assertIsDisplayed()

        compose.onNodeWithContentDescription("Search").performTextInput("child")
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("2 of 4") }
        compose.onNodeWithContentDescription("b-0002").assertIsDisplayed()

        compose.onNodeWithContentDescription("Search").performTextReplacement("")
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("4 of 4") }
        compose.onNodeWithContentDescription("Nested b-0002").assertIsDisplayed()
    }

    @Test
    fun aFilterLeavesTheParentAsContextWithoutCountingIt() {
        show()
        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithContentDescription("Status todo").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("2 of 4") }
        compose.onNodeWithContentDescription("Context b-0001").assertIsDisplayed()
        compose.onNodeWithContentDescription("Nested b-0002").assertIsDisplayed()
        compose.onNodeWithText("parent").assertIsDisplayed()
    }

    @Test
    fun theSortIsRememberedForTheRepository() {
        val model = show()
        compose.onNodeWithContentDescription("Sort, Default order, Ascending").performClick()
        compose.onNodeWithContentDescription("Sort by Updated").performClick()
        compose.waitUntil(5_000) { model.query.value.sort == BeanSort.Updated }

        val stored = runBlocking { catalog.current().active!! }
        assertEquals(BeanSort.Updated, stored.sort)

        val reopened = AppViewModel(beans)
        compose.waitUntil(5_000) { reopened.query.value.sort == BeanSort.Updated }
        assertEquals(BeanSort.Updated, reopened.query.value.sort)
    }

    @Test
    fun priorityIsMarkedOnlyAtTheEnds() {
        val url = TestRepo.remote(
            scratch,
            "prio",
            listOf(
                Triple("p-0001", "Urgent", TestRepo.bean("Urgent", status = "todo").replace("type: task", "type: task\npriority: high")),
                Triple("p-0002", "Later", TestRepo.bean("Later", status = "todo").replace("type: task", "type: task\npriority: low")),
                Triple("p-0003", "Plain", TestRepo.bean("Plain", status = "todo")),
            ),
        )
        runBlocking {
            val config = catalog.add(url, "Prio", null)
            catalog.activate(config.id)
            beans.loadActive()
        }
        show()
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("3 of 3") }
        compose.onNodeWithContentDescription("Raised priority, high").assertIsDisplayed()
        compose.onNodeWithContentDescription("Lowered priority, low").assertIsDisplayed()
    }
}
