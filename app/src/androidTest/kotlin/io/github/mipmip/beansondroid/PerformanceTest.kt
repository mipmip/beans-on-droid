package io.github.mipmip.beansondroid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.data.BeanLoader
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.data.IndexState
import io.github.mipmip.beansondroid.index.BeanQuery
import io.github.mipmip.beansondroid.repo.AndroidGit
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.ui.BeansNavHost
import io.github.mipmip.beansondroid.ui.onAllNodesWithTextSafely
import io.github.mipmip.beansondroid.ui.screen.BEAN_LIST_TAG
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.system.measureTimeMillis

private const val BEAN_COUNT = 600

class PerformanceTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var workTree: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AndroidGit.install(File(context.filesDir, "git"))
        scratch = File(context.cacheDir, "perf-${System.nanoTime()}").apply { mkdirs() }
        workTree = File(scratch, "origin").apply { mkdirs() }
        generate()
    }

    private fun generate() {
        Git.init().setDirectory(workTree).setInitialBranch("main").call().use { git ->
            val beans = File(workTree, ".beans").apply { mkdirs() }
            File(workTree, ".beans.yml").writeText("beans:\n    path: .beans\n")
            val statuses = listOf("todo", "in-progress", "draft", "completed")
            val types = listOf("task", "feature", "bug", "epic")
            repeat(BEAN_COUNT) { i ->
                val id = "perf-%04d".format(i)
                val parent = if (i > 0 && i % 10 != 0) "perf-%04d".format(i / 10 * 10) else null
                File(beans, "$id--bean-$i.md").writeText(
                    buildString {
                        appendLine("---")
                        appendLine("title: Generated bean number $i")
                        appendLine("status: ${statuses[i % statuses.size]}")
                        appendLine("type: ${types[i % types.size]}")
                        appendLine("priority: normal")
                        appendLine("tags:")
                        appendLine("    - tag${i % 12}")
                        appendLine("created_at: 2026-01-0${i % 9 + 1}T03:04:05Z")
                        appendLine("order: ${"%04d".format(i)}")
                        parent?.let { appendLine("parent: $it") }
                        appendLine("---")
                        appendLine()
                        appendLine("## Bean $i")
                        appendLine()
                        appendLine("Body paragraph for bean $i. " + "Filler sentence. ".repeat(20))
                        appendLine()
                        appendLine("- [ ] first task")
                        appendLine("- [x] second task")
                    },
                )
            }
            git.add().addFilepattern(".").call()
            git.commit().setMessage("generated beans").setSign(false).call()
        }
    }

    @Test
    fun parsingAndIndexingSixHundredBeansIsFast() {
        val store = RepoStore(File(scratch, "repos"))
        runBlocking { store.clone("perf", workTree.toURI().toString()) }
        val beanDir = File(store.workingDir("perf"), ".beans")

        val loader = BeanLoader()
        var loaded = loader.load(beanDir)
        val elapsed = measureTimeMillis { loaded = loader.load(beanDir) }

        assertEquals(BEAN_COUNT, loaded.index.size)
        assertTrue(loaded.skipped.isEmpty())
        report("parse and index $BEAN_COUNT beans", elapsed)
        assertTrue("parse and index took ${elapsed}ms", elapsed < 4_000)
    }

    @Test
    fun queryingSixHundredBeansIsFast() {
        val store = RepoStore(File(scratch, "repos"))
        runBlocking { store.clone("perf", workTree.toURI().toString()) }
        val index = BeanLoader().load(File(store.workingDir("perf"), ".beans")).index

        index.query(BeanQuery(term = "warm"))

        val search = measureTimeMillis { repeat(20) { index.query(BeanQuery(term = "number 4")) } }
        val filter = measureTimeMillis {
            repeat(20) { index.query(BeanQuery(statuses = setOf("todo"), types = setOf("epic"))) }
        }
        val relations = measureTimeMillis { repeat(20) { index.relations("perf-0010") } }

        report("20 searches", search)
        report("20 filter queries", filter)
        report("20 relationship lookups", relations)

        assertTrue("search took ${search}ms for 20 runs", search < 2_000)
        assertTrue("filtering took ${filter}ms for 20 runs", filter < 1_000)
        assertTrue("relationships took ${relations}ms for 20 runs", relations < 1_000)
    }

    @Test
    fun theListScrollsAndSearchesWithSixHundredBeans() {
        val repository = BeansRepository(TestCatalog(), RepoStore(File(scratch, "repos")))
        runBlocking {
            repository.addRepository(workTree.toURI().toString(), "Perf", null)
        }
        assertTrue(repository.indexState.value is IndexState.Ready)

        val model = AppViewModel(repository) { null }
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeansNavHost(viewModel = model)
            }
        }

        compose.waitUntil(20_000) { compose.onAllNodesWithTextSafely("600 of 600") }

        val scroll = measureTimeMillis {
            compose.onNodeWithTag(BEAN_LIST_TAG).performScrollToIndex(BEAN_COUNT - 1)
            compose.waitForIdle()
        }
        report("scroll to the last of $BEAN_COUNT rows", scroll)

        val typing = measureTimeMillis {
            compose.onNodeWithContentDescription("Search").performTextInput("number 42")
            compose.waitUntil(10_000) { compose.onAllNodesWithTextSafely("1 of 600") }
        }
        report("search narrowing $BEAN_COUNT beans to one", typing)

        compose.onNodeWithContentDescription("Search").performTextReplacement("")
        compose.waitUntil(10_000) { compose.onAllNodesWithTextSafely("600 of 600") }

        assertTrue("scrolling took ${scroll}ms", scroll < 10_000)
        assertTrue("search took ${typing}ms", typing < 5_000)
    }

    private fun report(what: String, millis: Long) {
        android.util.Log.i("BeansPerf", "$what: ${millis}ms")
    }
}
