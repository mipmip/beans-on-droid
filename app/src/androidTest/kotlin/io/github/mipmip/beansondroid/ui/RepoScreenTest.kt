package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.TestCatalog
import io.github.mipmip.beansondroid.TestRepo
import io.github.mipmip.beansondroid.ui.screen.RepoScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class RepoScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var catalog: TestCatalog

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "repo-screen-${System.nanoTime()}").apply { mkdirs() }
        catalog = TestCatalog()
    }

    private fun show(model: AppViewModel) {
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                RepoScreen(viewModel = model, onBack = {})
            }
        }
    }

    private fun model(acceptAnyUrl: Boolean = false): AppViewModel {
        val repository = TestRepo.beansRepository(scratch, catalog)
        return if (acceptAnyUrl) {
            AppViewModel(repository) { null }
        } else {
            AppViewModel(repository)
        }
    }

    @Test
    fun emptyStateExplainsWhatToDo() {
        show(model())
        compose.onNodeWithText("No repositories yet").assertIsDisplayed()
    }

    @Test
    fun theFormRejectsAnSshUrlWithoutCloning() {
        show(model())
        compose.onNodeWithContentDescription("Add repository").performClick()
        compose.onNodeWithContentDescription("Clone URL")
            .performTextInput("git@github.com:hmans/beans.git")
        compose.onNodeWithText("Add").performClick()

        compose.onNodeWithText("SSH URLs are not supported. Use the https:// clone URL.")
            .assertIsDisplayed()
        assertTrue(runBlocking { catalog.current().repos.isEmpty() })
    }

    @Test
    fun addingARepositoryShowsItInTheList() {
        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(Triple("x-0001", "First", TestRepo.bean("First"))),
        )
        show(model(acceptAnyUrl = true))

        compose.onNodeWithContentDescription("Add repository").performClick()
        compose.onNodeWithContentDescription("Clone URL").performTextInput(url)
        compose.onNodeWithContentDescription("Name").performTextInput("Demo")
        compose.onNodeWithText("Add").performClick()

        compose.waitUntil(10_000) {
            compose.onAllNodesWithTextSafely("Demo")
        }
        compose.onNodeWithText("Demo").assertIsDisplayed()
        compose.onNodeWithContentDescription("Active repository").assertIsDisplayed()
    }

    @Test
    fun removingARepositoryAsksFirstAndThenRemovesIt() {
        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(Triple("x-0001", "First", TestRepo.bean("First"))),
        )
        val model = model(acceptAnyUrl = true)
        runBlocking { catalog.add(url, "Demo", null) }
        show(model)

        compose.onNodeWithContentDescription("Remove Demo").performClick()
        compose.onNodeWithText("Remove Demo?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Demo").assertIsDisplayed()

        compose.onNodeWithContentDescription("Remove Demo").performClick()
        compose.onNodeWithText("Remove").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTextSafely("No repositories yet")
        }
    }

    @Test
    fun aRepositoryWithATokenIsMarked() {
        runBlocking { catalog.add("https://example.test/private.git", "Private", "ghp_x") }
        show(model())
        compose.onNodeWithContentDescription("Uses a token").assertIsDisplayed()
    }
}
