package io.github.mipmip.beansondroid

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.repo.AndroidGit
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.ui.BeansNavHost
import io.github.mipmip.beansondroid.ui.screen.BEAN_LIST_TAG
import io.github.mipmip.beansondroid.ui.onAllNodesWithTextSafely
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Drives the whole app, from an empty install to reading a bean, against a git
 * repository served over HTTP on loopback.
 */
class EndToEndTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var workTree: File
    private var server: GitHttpServer? = null

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AndroidGit.install(File(context.filesDir, "git"))
        scratch = File(context.cacheDir, "e2e-${System.nanoTime()}").apply { mkdirs() }
        workTree = File(scratch, "origin").apply { mkdirs() }

        Git.init().setDirectory(workTree).setInitialBranch("main").call().use { git ->
            File(workTree, ".beans").mkdirs()
            File(workTree, ".beans.yml").writeText("beans:\n    path: .beans\n")
            writeBean("b-0001--parent-epic.md", TestRepo.bean("Parent epic", type = "epic", tags = listOf("core")))
            writeBean(
                "b-0002--child-task.md",
                TestRepo.bean(
                    "Child task",
                    status = "in-progress",
                    type = "task",
                    tags = listOf("core"),
                    parent = "b-0001",
                    body = "## Notes\n\nThe child task body mentions marmalade.",
                ),
            )
            writeBean("b-0003--unrelated.md", TestRepo.bean("Unrelated bug", status = "draft", type = "bug"))
            git.add().addFilepattern(".").call()
            git.commit().setMessage("initial beans").setSign(false).call()
        }
    }

    @After
    fun tearDown() {
        server?.stop()
    }

    private fun writeBean(name: String, content: String) {
        File(workTree, ".beans/$name").writeText(content)
    }

    private fun commit(name: String, content: String, message: String) {
        Git.open(workTree).use { git ->
            writeBean(name, content)
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).setSign(false).call()
        }
    }

    private fun launch(token: String? = null): String {
        val running = GitHttpServer(workTree, requiredToken = token).start()
        server = running
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = BeansRepository(TestCatalog(), RepoStore(File(scratch, "repos")))
        val model = AppViewModel(repository)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeansNavHost(viewModel = model)
            }
        }
        return running.url
    }

    private fun addRepository(url: String, token: String? = null) {
        compose.onNodeWithContentDescription("Repositories").performClick()
        compose.onNodeWithContentDescription("Add repository").performClick()
        compose.onNodeWithContentDescription("Clone URL").performTextInput(url)
        compose.onNodeWithContentDescription("Name").performTextInput("Demo")
        token?.let { compose.onNodeWithContentDescription("Access token").performTextInput(it) }
        compose.onNodeWithText("Add").performClick()
    }

    private fun waitForText(text: String, timeoutMs: Long = 20_000) {
        compose.waitUntil(timeoutMs) { compose.onAllNodesWithTextSafely(text) }
    }

    @Test
    fun addingARepositoryOverHttpLandsOnAPopulatedList() {
        val url = launch()
        addRepository(url)

        waitForText("Demo")
        compose.onNodeWithContentDescription("Back").performClick()

        waitForText("Parent epic")
        compose.onNodeWithText("Parent epic").assertIsDisplayed()
        compose.onNodeWithText("Child task").assertIsDisplayed()
        compose.onNodeWithText("3 of 3").assertIsDisplayed()
    }

    @Test
    fun searchAndFiltersNarrowTheList() {
        val url = launch()
        addRepository(url)
        waitForText("Demo")
        compose.onNodeWithContentDescription("Back").performClick()
        waitForText("Parent epic")

        compose.onNodeWithContentDescription("Search").performTextInput("marmalade")
        waitForText("1 of 3")
        compose.onNodeWithText("Child task").assertIsDisplayed()

        compose.onNodeWithContentDescription("Search").performTextReplacement("")
        waitForText("3 of 3")

        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithContentDescription("Type bug").performClick()
        waitForText("1 of 3")
        compose.onNodeWithText("Unrelated bug").assertIsDisplayed()
    }

    @Test
    fun openingABeanAndFollowingARelationshipLandsOnTheRightBean() {
        val url = launch()
        addRepository(url)
        waitForText("Demo")
        compose.onNodeWithContentDescription("Back").performClick()
        waitForText("Child task")

        compose.onNodeWithText("Child task").performClick()
        waitForText("Related beans")
        compose.onNodeWithContentDescription("Parent b-0001").performClick()

        waitForText("Parent epic")
        compose.onNodeWithContentDescription("Child b-0002").assertIsDisplayed()
        compose.onNodeWithText("b-0001").assertIsDisplayed()
    }

    @Test
    fun pullToRefreshPicksUpANewCommit() {
        val url = launch()
        addRepository(url)
        waitForText("Demo")
        compose.onNodeWithContentDescription("Back").performClick()
        waitForText("3 of 3")

        commit("b-0004--added-later.md", TestRepo.bean("Added later"), "one more bean")

        compose.onNodeWithTag(BEAN_LIST_TAG).performTouchInput {
            swipeDown(startY = top + 10f, endY = bottom - 10f, durationMillis = 400)
        }

        waitForText("4 of 4")
        compose.onNodeWithText("Added later").assertIsDisplayed()
    }

    @Test
    fun aBadTokenSurfacesTheAuthenticationError() {
        val url = launch(token = "correct-token")
        addRepository(url, token = "wrong-token")

        waitForText("Could not sign in")
        compose.onNodeWithText("Could not sign in", substring = true).assertIsDisplayed()
    }

    @Test
    fun theRightTokenGetsThrough() {
        val url = launch(token = "correct-token")
        addRepository(url, token = "correct-token")

        waitForText("Demo")
        compose.onNodeWithContentDescription("Back").performClick()
        waitForText("Parent epic")
    }
}
