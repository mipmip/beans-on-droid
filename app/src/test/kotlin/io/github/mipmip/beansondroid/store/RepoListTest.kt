package io.github.mipmip.beansondroid.store

import io.github.mipmip.beansondroid.index.BeanSort
import io.github.mipmip.beansondroid.index.SortDirection
import kotlinx.serialization.json.Json
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
    fun sortIsRecordedPerRepository() {
        val list = RepoList().add(repo("a")).add(repo("b"))
            .withSort("b", BeanSort.Updated, SortDirection.Ascending)
        assertEquals(BeanSort.Default, list.repos.first { it.id == "a" }.sort)
        assertNull(list.repos.first { it.id == "a" }.direction)
        assertEquals(BeanSort.Updated, list.repos.first { it.id == "b" }.sort)
        assertEquals(SortDirection.Ascending, list.repos.first { it.id == "b" }.direction)
    }

    @Test
    fun anUnknownIdLeavesTheSortAlone() {
        val list = RepoList().add(repo("a")).withSort("ghost", BeanSort.Title, null)
        assertEquals(BeanSort.Default, list.repos.single().sort)
    }

    @Test
    fun aStoredListWrittenBeforeSortExistedDecodesWithTheDefault() {
        val older = """{"repos":[{"id":"a","url":"https://example.test/a.git",""" +
            """"label":"a","hasToken":false}],"activeId":"a"}"""
        val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<RepoList>(older)
        assertEquals(1, decoded.repos.size)
        assertEquals(BeanSort.Default, decoded.repos.single().sort)
        assertNull(decoded.repos.single().direction)
    }

    @Test
    fun aSortRoundTripsThroughJson() {
        val json = Json { ignoreUnknownKeys = true }
        val list = RepoList().add(repo("a")).withSort("a", BeanSort.Priority, SortDirection.Descending)
        val decoded = json.decodeFromString<RepoList>(json.encodeToString(list))
        assertEquals(BeanSort.Priority, decoded.repos.single().sort)
        assertEquals(SortDirection.Descending, decoded.repos.single().direction)
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
