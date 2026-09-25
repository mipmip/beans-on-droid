package io.github.mipmip.beansondroid.repo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class JGitRuntimeTest {

    private lateinit var scratch: File
    private lateinit var store: RepoStore

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AndroidGit.install(File(context.filesDir, "git"))
        scratch = File(context.cacheDir, "jgit-runtime").apply {
            deleteRecursively()
            mkdirs()
        }
        store = RepoStore(File(scratch, "repos"))
    }

    private fun remote(): File {
        val dir = File(scratch, "remote").apply { mkdirs() }
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, ".beans").mkdirs()
            File(dir, ".beans.yml").writeText("beans:\n    path: .beans\n")
            File(dir, ".beans/x-aaaa--on-device.md").writeText(
                "---\ntitle: Cloned on a device\nstatus: todo\ntype: task\n---\n\nBody.\n",
            )
            git.add().addFilepattern(".").call()
            git.commit().setMessage("first").setSign(false).call()
        }
        return dir
    }

    @Test
    fun jgitClonesOnAndroid() {
        val url = remote().toURI().toString()
        val result = runBlocking { store.clone("device", url) }
        assertTrue("$result", result is RepoResult.Success)
        assertTrue(File(store.workingDir("device"), ".beans/x-aaaa--on-device.md").isFile)
    }

    @Test
    fun jgitRefreshesOnAndroid() {
        val remoteDir = remote()
        val url = remoteDir.toURI().toString()
        runBlocking { store.clone("device", url) }

        Git.open(remoteDir).use { git ->
            File(remoteDir, ".beans/x-bbbb--later.md").writeText(
                "---\ntitle: Added later\nstatus: todo\ntype: task\n---\n",
            )
            git.add().addFilepattern(".").call()
            git.commit().setMessage("later").setSign(false).call()
        }

        val result = runBlocking { store.refresh("device") }
        assertTrue("$result", result is RepoResult.Success)
        assertTrue(File(store.workingDir("device"), ".beans/x-bbbb--later.md").isFile)
    }

    @Test
    fun shallowCloneAndRefreshWorkOverHttp() {
        val remoteDir = remote()
        val server = io.github.mipmip.beansondroid.GitHttpServer(remoteDir).start()
        try {
            val cloned = runBlocking { store.clone("http", server.url) }
            assertTrue("$cloned", cloned is RepoResult.Success)
            assertTrue(File(store.workingDir("http"), ".beans/x-aaaa--on-device.md").isFile)

            Git.open(remoteDir).use { git ->
                File(remoteDir, ".beans/x-cccc--over-http.md").writeText(
                    "---\ntitle: Over http\nstatus: todo\ntype: task\n---\n",
                )
                git.add().addFilepattern(".").call()
                git.commit().setMessage("over http").setSign(false).call()
            }

            val refreshed = runBlocking { store.refresh("http") }
            assertTrue("$refreshed", refreshed is RepoResult.Success)
            assertTrue(File(store.workingDir("http"), ".beans/x-cccc--over-http.md").isFile)
        } finally {
            server.stop()
        }
    }

    @Test
    fun beansAreParsedFromAClonedRepositoryOnDevice() {
        runBlocking { store.clone("device", remote().toURI().toString()) }
        val beanDir = store.beanDirectory("device").valueOrNull()!!
        val files = BeansLayout.beanFiles(beanDir)
        assertEquals(1, files.size)

        val parser = io.github.mipmip.beansondroid.bean.BeanParser()
        val parsed = parser.parseFile(files.first().first)
        assertTrue("$parsed", parsed is io.github.mipmip.beansondroid.bean.ParseResult.Parsed)
        assertEquals(
            "Cloned on a device",
            (parsed as io.github.mipmip.beansondroid.bean.ParseResult.Parsed).bean.title,
        )
    }
}
