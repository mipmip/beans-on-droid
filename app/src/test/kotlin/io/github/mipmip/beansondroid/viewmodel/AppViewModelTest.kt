package io.github.mipmip.beansondroid.viewmodel

import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.data.FakeRepoCatalog
import io.github.mipmip.beansondroid.data.IndexState
import io.github.mipmip.beansondroid.repo.RepoError
import io.github.mipmip.beansondroid.repo.RepoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()
    private lateinit var catalog: FakeRepoCatalog
    private lateinit var beans: BeansRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        catalog = FakeRepoCatalog()
        beans = BeansRepository(catalog, RepoStore(temp.newFolder("repos"), dispatcher))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun model() = AppViewModel(beans, dispatcher)

    private fun modelAcceptingAnyUrl() = AppViewModel(beans, dispatcher) { null }

    private fun remote(name: String = "remote", beanCount: Int = 2): String {
        val dir = temp.newFolder(name)
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, ".beans").mkdirs()
            repeat(beanCount) { i ->
                File(dir, ".beans/x-000$i--bean-$i.md").writeText(
                    "---\ntitle: Bean $i\nstatus: ${if (i == 0) "todo" else "draft"}\n" +
                        "type: task\ntags:\n    - t$i\n---\n\nBody $i.\n",
                )
            }
            git.add().addFilepattern(".").call()
            git.commit().setMessage("beans").setSign(false).call()
        }
        return dir.toURI().toString()
    }

    @Test
    fun invalidUrlIsReportedWithoutTouchingTheRepository() = runTest(dispatcher) {
        val model = model()
        model.onAddRepoUrlChanged("git@github.com:hmans/beans.git")
        model.addRepository()
        advanceUntilIdle()

        assertTrue(model.addRepo.value.error!!.contains("SSH"))
        assertFalse(model.addRepo.value.busy)
        assertTrue(catalog.current().repos.isEmpty())
    }

    @Test
    fun emptyUrlIsReported() = runTest(dispatcher) {
        val model = model()
        model.addRepository()
        advanceUntilIdle()
        assertEquals("Enter the repository's clone URL.", model.addRepo.value.error)
    }

    @Test
    fun typingClearsThePreviousError() = runTest(dispatcher) {
        val model = model()
        model.addRepository()
        advanceUntilIdle()
        assertTrue(model.addRepo.value.error != null)

        model.onAddRepoUrlChanged("https://example.test/x.git")
        assertNull(model.addRepo.value.error)
    }

    @Test
    fun aFailedCloneLeavesTheFormUsable() = runTest(dispatcher) {
        val model = model()
        model.onAddRepoUrlChanged("http://127.0.0.1:1/x.git")
        model.addRepository()
        advanceUntilIdle()

        assertFalse(model.addRepo.value.busy)
        assertTrue(model.addRepo.value.error!!.contains("Could not reach"))
        assertEquals("http://127.0.0.1:1/x.git", model.addRepo.value.url)
    }

    @Test
    fun queryStateAccumulatesAndClears() = runTest(dispatcher) {
        val model = model()
        model.setTerm("parser")
        model.toggleStatus("todo")
        model.toggleType("epic")
        model.toggleTag("core")
        model.toggleArchived()

        assertEquals("parser", model.query.value.term)
        assertEquals(setOf("todo"), model.query.value.statuses)
        assertEquals(setOf("epic"), model.query.value.types)
        assertEquals(setOf("core"), model.query.value.tags)
        assertTrue(model.query.value.includeArchived)

        model.clearFilters()
        assertEquals("parser", model.query.value.term)
        assertTrue(model.query.value.statuses.isEmpty())
        assertFalse(model.query.value.includeArchived)
    }

    @Test
    fun togglingTwiceRemovesTheFilter() = runTest(dispatcher) {
        val model = model()
        model.toggleStatus("todo")
        model.toggleStatus("todo")
        assertTrue(model.query.value.statuses.isEmpty())
    }

    @Test
    fun resetAddRepoClearsTheForm() = runTest(dispatcher) {
        val model = model()
        model.onAddRepoUrlChanged("https://example.test/x.git")
        model.onAddRepoLabelChanged("X")
        model.onAddRepoTokenChanged("ghp_x")
        model.resetAddRepo()

        assertEquals(AddRepoState(), model.addRepo.value)
    }

    @Test
    fun addingARepositorySucceedsAndClearsTheForm() = runTest(dispatcher) {
        val model = modelAcceptingAnyUrl()
        var added = false
        model.onAddRepoUrlChanged(remote())
        model.onAddRepoLabelChanged("Remote")
        model.addRepository { added = true }
        advanceUntilIdle()

        assertTrue(added)
        assertEquals(AddRepoState(), model.addRepo.value)
        assertTrue(beans.indexState.value is IndexState.Ready)
    }

    @Test
    fun refreshFlagIsRaisedAndLowered() = runTest(dispatcher) {
        val model = modelAcceptingAnyUrl()
        model.onAddRepoUrlChanged(remote())
        model.addRepository()
        advanceUntilIdle()

        model.refresh()
        assertTrue(model.refreshing.value)
        advanceUntilIdle()
        assertFalse(model.refreshing.value)
    }

    @Test
    fun activateAndRemoveGoThroughToTheRepository() = runTest(dispatcher) {
        val model = modelAcceptingAnyUrl()
        model.onAddRepoUrlChanged(remote("one", 1))
        model.addRepository()
        advanceUntilIdle()
        model.onAddRepoUrlChanged(remote("two", 3))
        model.addRepository()
        advanceUntilIdle()

        val first = catalog.current().repos.first()
        model.activate(first.id)
        advanceUntilIdle()
        assertEquals(1, (beans.indexState.value as IndexState.Ready).beans.index.size)

        model.removeRepository(first.id)
        advanceUntilIdle()
        assertEquals(1, catalog.current().repos.size)
    }

    @Test
    fun retryReloadsTheActiveRepository() = runTest(dispatcher) {
        val model = modelAcceptingAnyUrl()
        model.onAddRepoUrlChanged(remote())
        model.addRepository()
        advanceUntilIdle()

        model.retry()
        advanceUntilIdle()
        assertTrue(beans.indexState.value is IndexState.Ready)
    }

    @Test
    fun errorsAreDescribedForPeople() {
        assertTrue(
            AppViewModel.describe(RepoError.Authentication("x")).contains("personal access token"),
        )
        assertTrue(AppViewModel.describe(RepoError.Network("x")).contains("Could not reach"))
        assertTrue(
            AppViewModel.describe(RepoError.NotABeansRepository("no .beans directory"))
                .contains("no beans in it"),
        )
        assertEquals("boom", AppViewModel.describe(RepoError.Unknown("boom")))
    }
}
