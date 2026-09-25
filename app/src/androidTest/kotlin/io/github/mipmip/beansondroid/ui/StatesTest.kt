package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.TestCatalog
import io.github.mipmip.beansondroid.TestRepo
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.ui.screen.BeanDetailScreen
import io.github.mipmip.beansondroid.ui.screen.BeanListScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class StatesTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var catalog: TestCatalog
    private lateinit var beans: BeansRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "states-${System.nanoTime()}").apply { mkdirs() }
        catalog = TestCatalog()
        beans = TestRepo.beansRepository(scratch, catalog)
    }

    private fun showList() {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeanListScreen(viewModel = model, onOpenBean = {}, onOpenRepos = {})
            }
        }
    }

    private fun showDetail(id: String) {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeanDetailScreen(viewModel = model, beanId = id, onOpenBean = {}, onBack = {})
            }
        }
    }

    private fun activate(url: String) = runBlocking {
        val config = catalog.add(url, "Demo", null)
        catalog.activate(config.id)
        beans.loadActive()
    }

    @Test
    fun noRepositoryOffersToAddOne() {
        showList()
        compose.onNodeWithText("No repository yet").assertIsDisplayed()
        compose.onNodeWithText("Add a repository").assertIsDisplayed()
    }

    @Test
    fun aRepositoryWithNoBeansSaysSo() {
        activate(TestRepo.remote(scratch, "empty", emptyList()))
        showList()
        compose.onNodeWithText("This repository has no beans").assertIsDisplayed()
    }

    @Test
    fun aMissingBeanDirectoryIsItsOwnMessage() {
        val dir = File(scratch, "nobeans").apply { mkdirs() }
        org.eclipse.jgit.api.Git.init().setDirectory(dir).setInitialBranch("main").call()
            .use { git ->
                File(dir, "README.md").writeText("nothing here")
                git.add().addFilepattern(".").call()
                git.commit().setMessage("init").setSign(false).call()
            }
        activate(dir.toURI().toString())
        showList()
        compose.onNodeWithText("no beans in it", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun anUnreachableRepositoryReportsANetworkFailureWithRetry() {
        runBlocking {
            val config = catalog.add("https://beans.invalid/x.git", "Broken", null)
            catalog.activate(config.id)
            beans.loadActive()
        }
        showList()
        compose.waitUntil(10_000) { compose.onAllNodesWithTextSafely("Could not reach") }
        compose.onNodeWithText("Could not reach", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun unreadableFilesAreCountedAndListed() {
        val url = TestRepo.remote(
            scratch,
            "mixed",
            listOf(
                Triple("x-0001", "Good", TestRepo.bean("Good")),
                Triple("x-0002", "Bad", "---\ntitle: Bad\n  status: todo\n tags: [oops\n---\n\nBody.\n"),
            ),
        )
        activate(url)
        showList()

        compose.onNodeWithText("1 of 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Files that could not be read").performClick()
        compose.onNodeWithText("x-0002--bad.md").assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("Good").assertIsDisplayed()
    }

    @Test
    fun aFailedRefreshKeepsTheBeansAndOffersRetry() {
        val url = TestRepo.remote(
            scratch,
            "remote",
            listOf(Triple("x-0001", "Still here", TestRepo.bean("Still here"))),
        )
        activate(url)
        File(java.net.URI(url)).deleteRecursively()
        runBlocking { beans.refresh() }

        showList()
        compose.onNodeWithContentDescription("Refresh failed").assertIsDisplayed()
        compose.onNodeWithText("Still here").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun theDetailScreenDoesNotClaimAMissingBeanWhenNothingIsLoaded() {
        showDetail("x-0001")
        compose.onNodeWithText("Not ready yet").assertIsDisplayed()
        compose.onNodeWithText("No repository is open.").assertIsDisplayed()
    }
}
