package io.github.mipmip.beansondroid.data

import io.github.mipmip.beansondroid.repo.RepoError
import io.github.mipmip.beansondroid.repo.RepoResult
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.repo.errorOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BeansRepositoryTest {

    @get:Rule
    val temp = TemporaryFolder()

    private lateinit var catalog: FakeRepoCatalog
    private lateinit var store: RepoStore
    private lateinit var beans: BeansRepository

    @Before
    fun setUp() {
        catalog = FakeRepoCatalog()
        store = RepoStore(temp.newFolder("repos"), Dispatchers.Unconfined)
        beans = BeansRepository(catalog, store)
    }

    private fun remote(name: String = "remote", beanCount: Int = 2): String {
        val dir = temp.newFolder(name)
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, ".beans").mkdirs()
            repeat(beanCount) { i ->
                File(dir, ".beans/x-000$i--bean-$i.md").writeText(
                    "---\ntitle: Bean $i\nstatus: todo\ntype: task\n---\n\nBody $i.\n",
                )
            }
            git.add().addFilepattern(".").call()
            git.commit().setMessage("beans").setSign(false).call()
        }
        return dir.toURI().toString()
    }

    @Test
    fun startsWithNoRepository() {
        assertEquals(IndexState.NoRepository, beans.indexState.value)
    }

    @Test
    fun addingARepositoryClonesAndIndexes() = runBlocking {
        val result = beans.addRepository(remote(), "Remote", null)
        assertTrue("$result", result is RepoResult.Success)

        val state = beans.indexState.value
        assertTrue("$state", state is IndexState.Ready)
        assertEquals(2, (state as IndexState.Ready).beans.index.size)
        assertEquals("Remote", state.repo.label)
    }

    @Test
    fun aFailedCloneDoesNotLeaveTheRepositoryInTheList() = runBlocking {
        val result = beans.addRepository("https://beans.invalid/x.git", "", null)
        assertTrue(result.errorOrNull() is RepoError.Network)
        assertTrue(catalog.current().repos.isEmpty())
        assertTrue(beans.indexState.value is IndexState.Failed)
    }

    @Test
    fun aRepositoryWithoutBeansFailsAfterCloning() = runBlocking {
        val dir = temp.newFolder("plain")
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, "README.md").writeText("no beans here")
            git.add().addFilepattern(".").call()
            git.commit().setMessage("init").setSign(false).call()
        }
        beans.addRepository(dir.toURI().toString(), "Plain", null)

        val state = beans.indexState.value
        assertTrue("$state", state is IndexState.Failed)
        assertTrue((state as IndexState.Failed).error is RepoError.NotABeansRepository)
    }

    @Test
    fun refreshPicksUpNewBeans() = runBlocking {
        val url = remote("remote", beanCount = 1)
        beans.addRepository(url, "Remote", null)
        assertEquals(1, (beans.indexState.value as IndexState.Ready).beans.index.size)

        val remoteDir = File(java.net.URI(url))
        Git.open(remoteDir).use { git ->
            File(remoteDir, ".beans/x-9999--added.md").writeText(
                "---\ntitle: Added later\nstatus: todo\ntype: task\n---\n",
            )
            git.add().addFilepattern(".").call()
            git.commit().setMessage("added").setSign(false).call()
        }

        beans.refresh()
        assertEquals(2, (beans.indexState.value as IndexState.Ready).beans.index.size)
    }

    @Test
    fun refreshWithoutARepositoryReportsNoRepository() = runBlocking {
        beans.refresh()
        assertEquals(IndexState.NoRepository, beans.indexState.value)
    }

    @Test
    fun switchingRepositoriesReindexes() = runBlocking {
        beans.addRepository(remote("one", beanCount = 1), "One", null)
        val two = beans.addRepository(remote("two", beanCount = 3), "Two", null)
        assertTrue(two is RepoResult.Success)
        assertEquals(3, (beans.indexState.value as IndexState.Ready).beans.index.size)

        val first = catalog.current().repos.first { it.label == "One" }
        beans.activate(first.id)
        assertEquals(1, (beans.indexState.value as IndexState.Ready).beans.index.size)
    }

    @Test
    fun removingTheActiveRepositoryFallsBackToTheOther() = runBlocking {
        beans.addRepository(remote("one", beanCount = 1), "One", null)
        beans.addRepository(remote("two", beanCount = 3), "Two", null)
        val active = catalog.current().active!!

        beans.removeRepository(active.id)
        val state = beans.indexState.value
        assertTrue("$state", state is IndexState.Ready)
        assertTrue((state as IndexState.Ready).repo.id != active.id)
    }

    @Test
    fun removingTheLastRepositoryLeavesNoRepository() = runBlocking {
        beans.addRepository(remote(), "Remote", null)
        beans.removeRepository(catalog.current().active!!.id)
        assertEquals(IndexState.NoRepository, beans.indexState.value)
    }

    @Test
    fun aFailedRefreshKeepsTheBeansOnScreenAndExplainsWhy() = runBlocking {
        val url = remote("remote", beanCount = 2)
        beans.addRepository(url, "Remote", null)
        assertTrue(beans.indexState.value is IndexState.Ready)

        File(java.net.URI(url)).deleteRecursively()
        beans.refresh()

        val state = beans.indexState.value
        assertTrue("$state", state is IndexState.Ready)
        assertEquals(2, (state as IndexState.Ready).beans.index.size)
        assertTrue(state.staleReason != null)
    }

    @Test
    fun aSuccessfulRefreshClearsAPreviousStaleMarker() = runBlocking {
        val url = remote("remote", beanCount = 1)
        beans.addRepository(url, "Remote", null)
        val remoteDir = File(java.net.URI(url))
        val moved = File(remoteDir.parentFile, "moved-away")

        remoteDir.renameTo(moved)
        beans.refresh()
        assertTrue((beans.indexState.value as IndexState.Ready).staleReason != null)

        moved.renameTo(remoteDir)
        beans.refresh()
        assertNull((beans.indexState.value as IndexState.Ready).staleReason)
    }

    @Test
    fun aFailedRefreshOnADifferentRepositoryStillFails() = runBlocking {
        val url = remote("remote", beanCount = 1)
        val config = catalog.add(url, "Remote", null)
        catalog.activate(config.id)
        File(java.net.URI(url)).deleteRecursively()

        beans.loadActive()
        assertTrue(beans.indexState.value is IndexState.Failed)
    }

    @Test
    fun loadActiveIfNeededDoesNotDisturbAStaleMarker() = runBlocking {
        val url = remote("remote", beanCount = 1)
        beans.addRepository(url, "Remote", null)
        File(java.net.URI(url)).deleteRecursively()
        beans.refresh()
        assertTrue((beans.indexState.value as IndexState.Ready).staleReason != null)

        beans.loadActiveIfNeeded()
        assertTrue((beans.indexState.value as IndexState.Ready).staleReason != null)
    }

    @Test
    fun loadActiveIfNeededLoadsWhenNothingIsShowing() = runBlocking {
        val config = catalog.add(remote(), "Remote", null)
        catalog.activate(config.id)

        beans.loadActiveIfNeeded()
        assertTrue(beans.indexState.value is IndexState.Ready)
    }

    @Test
    fun loadingAnActiveRepositoryThatWasNeverClonedClonesIt() = runBlocking {
        val url = remote()
        val config = catalog.add(url, "Remote", null)
        catalog.activate(config.id)

        beans.loadActive()
        assertTrue(beans.indexState.value is IndexState.Ready)
    }
}
