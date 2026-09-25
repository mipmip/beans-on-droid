package io.github.mipmip.beansondroid.repo

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RepoStoreTest {

    @get:Rule
    val temp = TemporaryFolder()

    private lateinit var store: RepoStore
    private lateinit var remote: LocalRemote

    @Before
    fun setUp() {
        store = RepoStore(temp.newFolder("repos"))
        remote = LocalRemote(temp.newFolder("remote")).init()
    }

    private fun clone(id: String = "one", url: String = remote.url) =
        runBlocking { store.clone(id, url) }

    @Test
    fun cloneProducesAWorkingCopy() {
        val dir = clone().valueOrNull()!!
        assertTrue(dir.isDirectory)
        assertTrue(File(dir, ".beans/x-aaaa--first.md").isFile)
        assertTrue(store.isCloned("one"))
    }

    @Test
    fun cloneFetchesOnlyTheLatestCommit() {
        remote.commit("second.txt", "two", "second")
        remote.commit("third.txt", "three", "third")
        assertEquals(3, remote.commitCount())

        clone()
        val local = org.eclipse.jgit.api.Git.open(store.workingDir("one"))
        local.use { assertEquals(1, it.log().call().count()) }
    }

    @Test
    fun twoRepositoriesDoNotCollide() {
        val other = LocalRemote(temp.newFolder("remote-two")).init()
        other.commit(".beans/x-bbbb--other.md", LocalRemote.bean("Other bean"), "other bean")

        clone("one")
        clone("two", other.url)

        assertTrue(File(store.workingDir("one"), ".beans/x-aaaa--first.md").isFile)
        assertTrue(File(store.workingDir("two"), ".beans/x-bbbb--other.md").isFile)
        assertFalse(File(store.workingDir("one"), ".beans/x-bbbb--other.md").isFile)
    }

    @Test
    fun refreshPicksUpANewCommit() {
        clone()
        remote.commit(".beans/x-cccc--later.md", LocalRemote.bean("Later bean"), "later")

        val result = runBlocking { store.refresh("one") }
        assertTrue("$result", result is RepoResult.Success)
        assertTrue(File(store.workingDir("one"), ".beans/x-cccc--later.md").isFile)
    }

    @Test
    fun refreshDiscardsLocalModifications() {
        clone()
        val file = File(store.workingDir("one"), ".beans/x-aaaa--first.md")
        file.writeText("locally mangled")

        runBlocking { store.refresh("one") }
        assertTrue(file.readText().contains("First bean"))
    }

    @Test
    fun refreshFollowsARewrittenRemote() {
        clone()
        remote.resetHardToNewRoot("rewritten")

        val result = runBlocking { store.refresh("one") }
        assertTrue("$result", result is RepoResult.Success)
        assertTrue(File(store.workingDir("one"), "unrelated.txt").isFile)
    }

    @Test
    fun refreshOfAnUnclonedRepositoryFails() {
        val error = runBlocking { store.refresh("absent") }.errorOrNull()
        assertTrue("$error", error is RepoError.Unknown)
    }

    @Test
    fun deleteRemovesOnlyThatRepository() {
        clone("one")
        clone("two")
        assertTrue(store.delete("one"))
        assertFalse(store.workingDir("one").exists())
        assertTrue(store.workingDir("two").exists())
    }

    @Test
    fun deleteOfAnAbsentRepositoryReportsNothingRemoved() {
        assertFalse(store.delete("never-cloned"))
    }

    @Test
    fun beanDirectoryIsFoundAfterClone() {
        clone()
        assertEquals(File(store.workingDir("one"), ".beans"), store.beanDirectory("one").valueOrNull())
    }

    @Test
    fun repositoryWithoutBeansIsReported() {
        val bare = LocalRemote(temp.newFolder("plain"))
        bare.init()
        File(bare.url.removePrefix("file:"), ".beans").deleteRecursively()
        File(bare.url.removePrefix("file:"), ".beans.yml").delete()
        org.eclipse.jgit.api.Git.open(File(bare.url.removePrefix("file:"))).use { git ->
            git.rm().addFilepattern(".beans").addFilepattern(".beans.yml").call()
            git.commit().setMessage("remove beans").setSign(false).call()
        }

        clone("plain", bare.url)
        val error = store.beanDirectory("plain").errorOrNull()
        assertTrue("$error", error is RepoError.NotABeansRepository)
    }

    @Test
    fun cloneOfAnUnreachableUrlFailsAsNetwork() {
        val result = runBlocking { store.clone("bad", "http://127.0.0.1:1/does-not-exist.git") }
        val error = result.errorOrNull()
        assertTrue("$error", error is RepoError.Network)
    }

    @Test
    fun failedCloneLeavesNothingBehind() {
        runBlocking { store.clone("bad", "http://127.0.0.1:1/does-not-exist.git") }
        assertFalse(store.workingDir("bad").exists())
    }

    @Test
    fun cloneOfANonRepositoryPathFails() {
        val notARepo = temp.newFolder("not-a-repo")
        val result = runBlocking { store.clone("nope", notARepo.toURI().toString()) }
        assertTrue("$result", result is RepoResult.Failure)
        assertFalse(store.workingDir("nope").exists())
    }
}
