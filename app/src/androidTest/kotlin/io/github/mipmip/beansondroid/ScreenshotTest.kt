package io.github.mipmip.beansondroid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.repo.AndroidGit
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.ui.BeansNavHost
import io.github.mipmip.beansondroid.ui.onAllNodesWithTextSafely
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Not an assertion test: it drives the app into the states worth showing and
 * writes PNGs the fastlane metadata uses. Run it with
 * `./scripts/screenshots.sh`, which pulls the files off the device.
 */
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AndroidGit.install(File(context.filesDir, "git"))
        scratch = File(context.cacheDir, "shots-${System.nanoTime()}").apply { mkdirs() }
        outputDir = File("/sdcard/beans-screenshots")
        shell("rm -rf ${outputDir.path}")
        shell("mkdir -p ${outputDir.path}")
    }

    private fun repositoryUrl(): String {
        val dir = File(scratch, "origin").apply { mkdirs() }
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            File(dir, ".beans").mkdirs()
            File(dir, ".beans.yml").writeText("beans:\n    path: .beans\n")
            fun bean(name: String, content: String) =
                File(dir, ".beans/$name").writeText(content)

            bean(
                "beans-4k2p--offline-first-sync.md",
                TestRepo.bean(
                    "Offline-first sync",
                    status = "in-progress",
                    type = "epic",
                    tags = listOf("sync"),
                    body = "Keep working without a connection and reconcile later.",
                ),
            )
            bean(
                "beans-9wq1--parse-frontmatter-leniently.md",
                TestRepo.bean(
                    "Parse frontmatter leniently",
                    status = "todo",
                    type = "task",
                    tags = listOf("parser"),
                    parent = "beans-4k2p",
                    body = buildString {
                        appendLine("## Why")
                        appendLine()
                        appendLine("A bean written by a **newer** version of the tool must still")
                        appendLine("open. Unknown fields are kept and shown raw.")
                        appendLine()
                        appendLine("- [x] Accept a scalar where a list is expected")
                        appendLine("- [x] Keep unrecognised keys")
                        appendLine("- [ ] Document the format")
                    },
                ),
            )
            bean(
                "beans-7tzb--crash-on-empty-body.md",
                TestRepo.bean(
                    "Crash on empty body",
                    status = "todo",
                    type = "bug",
                    tags = listOf("parser"),
                    blockedBy = listOf("beans-9wq1"),
                ),
            )
            bean(
                "beans-2mne--tag-filtering.md",
                TestRepo.bean("Tag filtering", status = "draft", type = "feature", tags = listOf("ui")),
            )
            bean(
                "beans-5xra--dark-theme.md",
                TestRepo.bean("Dark theme polish", status = "completed", type = "feature", tags = listOf("ui")),
            )
            git.add().addFilepattern(".").call()
            git.commit().setMessage("beans").setSign(false).call()
        }
        return dir.toURI().toString()
    }

    @Test
    fun captureScreenshots() {
        val repository = BeansRepository(TestCatalog(), RepoStore(File(scratch, "repos")))
        runBlocking { repository.addRepository(repositoryUrl(), "beans", null) }

        val model = AppViewModel(repository) { null }
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                BeansNavHost(viewModel = model)
            }
        }

        compose.waitUntil(20_000) { compose.onAllNodesWithTextSafely("5 of 5") }
        capture("1_list")

        compose.onNodeWithContentDescription("Filters").performClick()
        compose.waitForIdle()
        capture("2_filters")

        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithText("Parse frontmatter leniently").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTextSafely("Related beans") }
        capture("3_detail")

        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Repositories").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTextSafely("beans") }
        capture("4_repositories")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        Thread.sleep(300)
        shell("screencap -p ${outputDir.path}/$name.png")
    }

    /**
     * Runs as the shell user, which can write to shared storage. The app's own
     * directories are removed when the test APKs are uninstalled after the run,
     * so the files have to land somewhere that outlives it.
     */
    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(command)
            .use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use {
                    it.readBytes()
                }
            }
    }
}
