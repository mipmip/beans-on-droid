package io.github.mipmip.beansondroid.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlCaptureTest {

    private fun normalised(url: String) = UrlCapture.normalise(url)

    @Test
    fun textSurroundingAUrl() {
        assertEquals(
            "https://github.com/hmans/beans",
            UrlCapture.extract("Look at this https://github.com/hmans/beans nice one"),
        )
    }

    @Test
    fun textThatIsOnlyAUrl() {
        assertEquals(
            "https://github.com/hmans/beans",
            UrlCapture.extract("https://github.com/hmans/beans"),
        )
    }

    @Test
    fun trailingPunctuationIsNotPartOfTheAddress() {
        listOf(".", ",", ")", "]", "}", ">", "\"", "'", ";", ":", "!", "?").forEach { mark ->
            assertEquals(
                "punctuation $mark",
                "https://github.com/hmans/beans",
                UrlCapture.extract("see https://github.com/hmans/beans$mark"),
            )
        }
    }

    @Test
    fun theFirstOfSeveralAddresses() {
        assertEquals(
            "https://a.test/one",
            UrlCapture.extract("https://a.test/one and https://b.test/two"),
        )
    }

    @Test
    fun noAddress() {
        assertNull(UrlCapture.extract("nothing to see here"))
        assertNull(UrlCapture.extract(""))
        assertEquals(CaptureResult.NoUrl, UrlCapture.capture("git@github.com:hmans/beans.git"))
    }

    @Test
    fun httpIsAcceptedAsWellAsHttps() {
        assertEquals("http://a.test/x", UrlCapture.extract("http://a.test/x"))
    }

    @Test
    fun queryStringFromARepositoryPage() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans?tab=readme-ov-file"),
        )
    }

    @Test
    fun fragmentIsRemoved() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans#readme"),
        )
    }

    @Test
    fun aFileView() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans/tree/main/pkg"),
        )
    }

    @Test
    fun anIssuesPage() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans/issues/12"),
        )
    }

    @Test
    fun everyViewSegmentIsRecognised() {
        listOf(
            "tree/main", "blob/main/a.md", "raw/main/a.md", "src/branch/main",
            "commit/abc", "commits/main", "issues", "pull/4", "pulls",
            "merge_requests/2", "releases", "tags", "wiki", "actions",
            "compare/a...b", "branches", "settings",
        ).forEach { view ->
            assertEquals(
                "view $view",
                "https://forge.test/owner/repo",
                normalised("https://forge.test/owner/repo/$view"),
            )
        }
    }

    @Test
    fun gitlabSeparatesTheViewWithAMarker() {
        assertEquals(
            "https://gitlab.com/group/subgroup/proj",
            normalised("https://gitlab.com/group/subgroup/proj/-/tree/main"),
        )
    }

    @Test
    fun aGiteaSourceView() {
        assertEquals(
            "https://codeberg.org/owner/repo",
            normalised("https://codeberg.org/owner/repo/src/branch/main"),
        )
    }

    @Test
    fun aRepositoryNamedAfterAViewSegment() {
        assertEquals(
            "https://github.com/someone/issues",
            normalised("https://github.com/someone/issues"),
        )
        assertEquals(
            "https://github.com/someone/tree",
            normalised("https://github.com/someone/tree"),
        )
    }

    @Test
    fun anAddressThatAlreadyClones() {
        assertEquals(
            "https://github.com/hmans/beans.git",
            normalised("https://github.com/hmans/beans.git"),
        )
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans"),
        )
    }

    @Test
    fun trailingSlash() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://github.com/hmans/beans/"),
        )
    }

    @Test
    fun aHostTheRulesDoNotRecognise() {
        assertEquals(
            "https://git.example.test/team/repo",
            normalised("https://git.example.test/team/repo"),
        )
    }

    @Test
    fun aBareHost() {
        assertEquals("https://example.test", normalised("https://example.test"))
        assertEquals("https://example.test/", normalised("https://example.test/"))
    }

    @Test
    fun captureCombinesExtractionAndNormalisation() {
        val result = UrlCapture.capture("here you go https://github.com/hmans/beans/issues?q=x .")
        assertEquals(CaptureResult.Found("https://github.com/hmans/beans"), result)
    }

    @Test
    fun credentialsAreStrippedSoTheyAreNeverStored() {
        val result = UrlCapture.capture("https://user:ghp_secret@github.com/hmans/beans")
        assertEquals(CaptureResult.Found("https://github.com/hmans/beans"), result)
    }

    @Test
    fun aBareUsernameIsAlsoStripped() {
        assertEquals(
            "https://github.com/hmans/beans",
            normalised("https://someone@github.com/hmans/beans"),
        )
    }

    @Test
    fun credentialsOnAHostWithNoPath() {
        assertEquals("https://example.test", normalised("https://tok@example.test"))
    }
}
