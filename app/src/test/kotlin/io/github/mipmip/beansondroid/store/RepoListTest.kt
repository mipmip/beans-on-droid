package io.github.mipmip.beansondroid.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepoListTest {

    private fun repo(id: String, url: String = "https://example.test/$id.git") =
        RepoConfig(id = id, url = url, label = id)

    @Test
    fun firstRepositoryBecomesActive() {
        val list = RepoList().add(repo("a"))
        assertEquals("a", list.activeId)
        assertEquals("a", list.active?.id)
    }

    @Test
    fun secondRepositoryDoesNotStealActive() {
        val list = RepoList().add(repo("a")).add(repo("b"))
        assertEquals("a", list.activeId)
        assertEquals(2, list.repos.size)
    }

    @Test
    fun addingTheSameUrlTwiceKeepsOneEntry() {
        val list = RepoList()
            .add(repo("a", "https://example.test/same.git"))
            .add(repo("b", "https://example.test/same.git"))
        assertEquals(1, list.repos.size)
        assertEquals("a", list.activeId)
    }

    @Test
    fun addingTheSameUrlUpdatesTheLabel() {
        val list = RepoList()
            .add(RepoConfig("a", "https://example.test/same.git", "old"))
            .add(RepoConfig("b", "https://example.test/same.git", "new", hasToken = true))
        assertEquals("new", list.repos.single().label)
        assertTrue(list.repos.single().hasToken)
    }

    @Test
    fun activateSwitches() {
        val list = RepoList().add(repo("a")).add(repo("b")).activate("b")
        assertEquals("b", list.activeId)
    }

    @Test
    fun activateIgnoresAnUnknownId() {
        val list = RepoList().add(repo("a")).activate("ghost")
        assertEquals("a", list.activeId)
    }

    @Test
    fun removingTheActiveRepositoryPromotesAnother() {
        val list = RepoList().add(repo("a")).add(repo("b")).remove("a")
        assertEquals("b", list.activeId)
        assertEquals(1, list.repos.size)
    }

    @Test
    fun removingANonActiveRepositoryLeavesActiveAlone() {
        val list = RepoList().add(repo("a")).add(repo("b")).remove("b")
        assertEquals("a", list.activeId)
    }

    @Test
    fun removingTheLastRepositoryLeavesNoneActive() {
        val list = RepoList().add(repo("a")).remove("a")
        assertTrue(list.repos.isEmpty())
        assertNull(list.activeId)
        assertNull(list.active)
    }

    @Test
    fun tokenFlagIsUpdatedById() {
        val list = RepoList().add(repo("a")).add(repo("b")).withToken("b", true)
        assertFalse(list.repos.first { it.id == "a" }.hasToken)
        assertTrue(list.repos.first { it.id == "b" }.hasToken)
    }

    @Test
    fun idIsStableForTheSameUrlAndIgnoresCaseAndSpace() {
        assertEquals(
            repoIdFor("https://example.test/x.git"),
            repoIdFor("  HTTPS://EXAMPLE.TEST/X.GIT  "),
        )
    }

    @Test
    fun idDiffersBetweenUrls() {
        assertTrue(repoIdFor("https://a.test/x.git") != repoIdFor("https://b.test/x.git"))
    }

    @Test
    fun labelIsDerivedFromTheUrl() {
        assertEquals("beans", labelFor("https://github.com/hmans/beans.git"))
        assertEquals("beans", labelFor("https://github.com/hmans/beans/"))
        assertEquals("beans", labelFor("https://github.com/hmans/beans"))
    }

    @Test
    fun repoConfigTextDoesNotCarryAToken() {
        val text = RepoConfig("a", "https://example.test/x.git", "x", hasToken = true).toString()
        assertTrue(text.contains("hasToken=true"))
        assertFalse(text.contains("ghp_"))
    }
}
