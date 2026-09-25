package io.github.mipmip.beansondroid.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    private lateinit var scratch: File
    private val scopes = mutableListOf<CoroutineScope>()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "persistence-${System.nanoTime()}").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        scopes.clear()
        scratch.deleteRecursively()
    }

    private fun storeAt(file: File): DataStore<Preferences> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scopes += scope
        return PreferenceDataStoreFactory.create(scope = scope) { file }
    }

    private fun store(name: String): DataStore<Preferences> =
        storeAt(File(scratch, "$name.preferences_pb"))

    private fun closeAll() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }

    private fun vault() = KeystoreTokenVault(store("tokens"))

    @Test
    fun tokenRoundTrips() = runBlocking {
        val vault = vault()
        vault.put("repo", "ghp_secret_value")
        assertEquals("ghp_secret_value", vault.get("repo"))
    }

    @Test
    fun storedBytesDoNotContainThePlainToken() = runBlocking {
        val file = File(scratch, "leak.preferences_pb")
        val vault = KeystoreTokenVault(storeAt(file))
        vault.put("repo", "ghp_secret_value")
        assertTrue(file.exists())
        val bytes = file.readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(bytes.contains("ghp_secret_value"))
    }

    @Test
    fun tokensAreIndependent() = runBlocking {
        val vault = vault()
        vault.put("one", "token-one")
        vault.put("two", "token-two")
        assertEquals("token-one", vault.get("one"))
        assertEquals("token-two", vault.get("two"))
    }

    @Test
    fun absentTokenIsNull() = runBlocking {
        assertNull(vault().get("never-stored"))
    }

    @Test
    fun repositoryListSurvivesANewRegistryOverTheSameFile() = runBlocking {
        val file = File(scratch, "repos.preferences_pb")
        val vault = InMemoryTokenVault()

        RepoRegistry(storeAt(file), vault).add("https://example.test/one.git", "", null)
        closeAll()

        val reopened = RepoRegistry(storeAt(file), vault).current()
        assertEquals(1, reopened.repos.size)
        assertEquals("https://example.test/one.git", reopened.repos.single().url)
        assertEquals("one", reopened.repos.single().label)
    }

    @Test
    fun listIsObservable() = runBlocking {
        val registry = RepoRegistry(store("repos"), InMemoryTokenVault())
        assertTrue(registry.repos.first().repos.isEmpty())
        registry.add("https://example.test/one.git", "One", null)
        assertEquals(1, registry.repos.first().repos.size)
    }

    @Test
    fun removingARepositoryRemovesItsToken() = runBlocking {
        val registry = RepoRegistry(store("repos"), vault())
        val added = registry.add("https://example.test/one.git", "One", "ghp_one")
        assertEquals("ghp_one", registry.tokenFor(added.id))

        registry.remove(added.id)
        assertNull(registry.tokenFor(added.id))
        assertTrue(registry.current().repos.isEmpty())
    }

    @Test
    fun removingOneRepositoryLeavesTheOtherToken() = runBlocking {
        val registry = RepoRegistry(store("repos"), vault())
        val one = registry.add("https://example.test/one.git", "One", "ghp_one")
        val two = registry.add("https://example.test/two.git", "Two", "ghp_two")

        registry.remove(one.id)
        assertEquals("ghp_two", registry.tokenFor(two.id))
    }

    @Test
    fun activeRepositoryIsTrackedAcrossAddAndSwitch() = runBlocking {
        val registry = RepoRegistry(store("repos"), InMemoryTokenVault())
        val one = registry.add("https://example.test/one.git", "One", null)
        val two = registry.add("https://example.test/two.git", "Two", null)
        assertEquals(one.id, registry.current().activeId)

        registry.activate(two.id)
        assertEquals(two.id, registry.current().activeId)

        registry.remove(two.id)
        assertEquals(one.id, registry.current().activeId)
    }

    @Test
    fun addingWithATokenMarksTheRepository() = runBlocking {
        val registry = RepoRegistry(store("repos"), vault())
        val added = registry.add("https://example.test/private.git", "Private", "ghp_x")
        assertTrue(registry.current().repos.single { it.id == added.id }.hasToken)
    }
}
