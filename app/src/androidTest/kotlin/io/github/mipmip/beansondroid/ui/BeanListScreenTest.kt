package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
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
import io.github.mipmip.beansondroid.ui.screen.BeanListScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class BeanListScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var catalog: TestCatalog
    private lateinit var beans: BeansRepository
    private val opened = mutableListOf<String>()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "bean-list-${System.nanoTime()}").apply { mkdirs() }
        catalog = TestCatalog()
        beans = TestRepo.beansRepository(scratch, catalog)
        opened.clear()
    }

    private fun withRepository() {
        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(
                Triple("x-0001", "Write the parser", TestRepo.bean("Write the parser", status = "todo", type = "task", tags = listOf("core"))),
                Triple("x-0002", "Ship the index", TestRepo.bean("Ship the index", status = "in-progress", type = "epic", tags = listOf("core"))),
                Triple("x-0003", "Polish the theme", TestRepo.bean("Polish the theme", status = "draft", type = "epic", body = "Colours and shapes live here.")),
            ),
        )
        runBlocking {
            val config = catalog.add(url, "Demo", null)
            catalog.activate(config.id)
            beans.loadActive()
        }
    }

    private fun show() {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeanListScreen(
                    viewModel = model,
                    onOpenBean = { opened += it },
                    onOpenRepos = {},
                )
            }
        }
    }

    @Test
    fun withoutARepositoryItSaysSo() {
        show()
        compose.onNodeWithText("No repository yet").assertIsDisplayed()
    }

    @Test
    fun beansAreListedWithIdStatusAndType() {
        withRepository()
        show()
        compose.onNodeWithText("Write the parser").assertIsDisplayed()
        compose.onNodeWithText("x-0001").assertIsDisplayed()
        compose.onNodeWithText("todo").assertIsDisplayed()
        compose.onNodeWithText("in-progress").assertIsDisplayed()
        compose.onAllNodesWithText("epic").assertCountEquals(2)
        compose.onNodeWithText("3 of 3").assertIsDisplayed()
    }

    @Test
    fun searchNarrowsTheList() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Search").performTextInput("parser")
        compose.onNodeWithText("Write the parser").assertIsDisplayed()
        compose.onNodeWithText("1 of 3").assertIsDisplayed()
    }

    @Test
    fun searchMatchesTheBody() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Search").performTextInput("colours")
        compose.onNodeWithText("Polish the theme").assertIsDisplayed()
        compose.onNodeWithText("1 of 3").assertIsDisplayed()
    }

    @Test
    fun aSearchWithNoMatchOffersToClear() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Search").performTextInput("kangaroo")
        compose.onNodeWithText("Nothing matches").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search").performTextReplacement("")
        compose.onNodeWithText("3 of 3").assertIsDisplayed()
    }

    @Test
    fun filteringByTypeNarrowsTheList() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithContentDescription("Type epic").performClick()
        compose.onNodeWithText("2 of 3").assertIsDisplayed()
        compose.onNodeWithContentDescription("Clear filters").performClick()
        compose.onNodeWithText("3 of 3").assertIsDisplayed()
    }

    @Test
    fun filteringByStatusAndTypeCombines() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithContentDescription("Type epic").performClick()
        compose.onNodeWithContentDescription("Status draft").performClick()
        compose.onNodeWithText("1 of 3").assertIsDisplayed()
        compose.onNodeWithText("Polish the theme").assertIsDisplayed()
    }

    @Test
    fun filteringByTag() {
        withRepository()
        show()
        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithContentDescription("Tag core").performClick()
        compose.onNodeWithText("2 of 3").assertIsDisplayed()
    }

    @Test
    fun tappingABeanOpensIt() {
        withRepository()
        show()
        compose.onNodeWithText("Write the parser").performClick()
        assertEquals(listOf("x-0001"), opened)
    }
}
