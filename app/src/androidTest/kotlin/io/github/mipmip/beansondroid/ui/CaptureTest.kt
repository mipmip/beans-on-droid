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
import io.github.mipmip.beansondroid.ui.screen.RepoScreen
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class CaptureTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scratch: File
    private lateinit var catalog: TestCatalog
    private lateinit var beans: BeansRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scratch = File(context.cacheDir, "capture-${System.nanoTime()}").apply { mkdirs() }
        catalog = TestCatalog()
        beans = TestRepo.beansRepository(scratch, catalog)
    }

    private fun show(): AppViewModel {
        val model = AppViewModel(beans)
        compose.setContent {
            BeansOnDroidTheme(dynamicColor = false) {
                RepoScreen(viewModel = model, onBack = {})
            }
        }
        return model
    }

    @Test
    fun aCapturedPageUrlOpensTheFormWithTheCloneUrl() {
        val model = show()
        compose.runOnUiThread {
            model.captureUrl("https://github.com/hmans/beans/tree/main/pkg?tab=readme-ov-file")
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("Add repository") }
        assertEquals("https://github.com/hmans/beans", model.addRepo.value.url)
        compose.onNodeWithContentDescription("Clone URL").assertIsDisplayed()
    }

    @Test
    fun aCapturedUrlInsideOtherTextIsExtracted() {
        val model = show()
        compose.runOnUiThread {
            model.captureUrl("have a look at https://codeberg.org/owner/repo/src/branch/main .")
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("Add repository") }
        assertEquals("https://codeberg.org/owner/repo", model.addRepo.value.url)
    }

    @Test
    fun capturedTextWithNoUrlIsReported() {
        val model = show()
        compose.runOnUiThread { model.captureUrl("nothing useful here") }
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("No repository URL in that.") }
        compose.onNodeWithText("No repository URL in that.").assertIsDisplayed()
        assertEquals("", model.addRepo.value.url)
    }

    @Test
    fun aCapturedUrlIsNotClonedUntilConfirmed() {
        val model = show()
        compose.runOnUiThread { model.captureUrl("https://github.com/hmans/beans") }
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("Add repository") }

        assertTrue(runBlocking { catalog.current().repos.isEmpty() })
        compose.onNodeWithText("Cancel").performClick()
        assertTrue(runBlocking { catalog.current().repos.isEmpty() })
    }

    @Test
    fun credentialsInACapturedUrlAreDiscarded() {
        val model = show()
        compose.runOnUiThread {
            model.captureUrl("https://someone:ghp_secret@github.com/hmans/beans")
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTextSafely("Add repository") }
        assertEquals("https://github.com/hmans/beans", model.addRepo.value.url)
        assertEquals("", model.addRepo.value.token)
    }

    @Test
    fun theVersionIsShownWhenTheListIsEmpty() {
        show()
        compose.onNodeWithContentDescription(expectedVersionDescription()).assertIsDisplayed()
    }

    @Test
    fun theVersionIsShownWithRepositoriesConfigured() {
        runBlocking { catalog.add("https://example.test/a.git", "A", null) }
        show()
        compose.onNodeWithText("A").assertIsDisplayed()
        compose.onNodeWithContentDescription(expectedVersionDescription()).assertIsDisplayed()
    }

    @Test
    fun theShownVersionIsTheRunningBuild() {
        show()
        val expected = "Beans on Droid " +
            "${io.github.mipmip.beansondroid.BuildConfig.VERSION_NAME} " +
            "(${io.github.mipmip.beansondroid.BuildConfig.VERSION_CODE})"
        compose.onNodeWithText(expected).assertIsDisplayed()
    }

    private fun expectedVersionDescription(): String =
        "Version ${io.github.mipmip.beansondroid.BuildConfig.VERSION_NAME} " +
            "(${io.github.mipmip.beansondroid.BuildConfig.VERSION_CODE})"

    @Test
    fun theFormOffersScanAndPaste() {
        show()
        compose.onNodeWithContentDescription("Add repository").performClick()
        compose.onNodeWithContentDescription("Paste a URL").assertIsDisplayed()
        compose.onNodeWithContentDescription("Scan a QR code").assertIsDisplayed()
    }

    @Test
    fun pastingFillsTheFieldFromTheClipboard() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val model = show()
        compose.runOnUiThread {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
            clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText(
                    "url",
                    "https://github.com/hmans/beans/issues/3",
                ),
            )
        }
        compose.onNodeWithContentDescription("Add repository").performClick()
        compose.onNodeWithContentDescription("Paste a URL").performClick()
        compose.waitUntil(5_000) {
            model.addRepo.value.url == "https://github.com/hmans/beans"
        }
        assertEquals("https://github.com/hmans/beans", model.addRepo.value.url)
    }
}
