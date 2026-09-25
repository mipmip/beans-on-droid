package io.github.mipmip.beansondroid.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepoUrlTest {

    @Test
    fun httpsUrlIsAccepted() {
        assertNull(RepoUrl.validate("https://github.com/hmans/beans.git"))
    }

    @Test
    fun httpUrlIsAccepted() {
        assertNull(RepoUrl.validate("http://git.example.test/x.git"))
    }

    @Test
    fun surroundingSpaceIsIgnored() {
        assertNull(RepoUrl.validate("  https://github.com/hmans/beans.git  "))
    }

    @Test
    fun emptyUrlIsRejected() {
        assertEquals("Enter the repository's clone URL.", RepoUrl.validate("   "))
    }

    @Test
    fun sshUrlIsRejectedWithAnExplanation() {
        assertTrue(RepoUrl.validate("git@github.com:hmans/beans.git")!!.contains("SSH"))
        assertTrue(RepoUrl.validate("ssh://git@example.test/x.git")!!.contains("SSH"))
    }

    @Test
    fun otherSchemesAreRejected() {
        assertTrue(RepoUrl.validate("file:///tmp/x")!!.contains("https://"))
        assertTrue(RepoUrl.validate("github.com/hmans/beans")!!.contains("https://"))
    }

    @Test
    fun schemeWithNoHostIsRejected() {
        assertEquals("That URL has no host.", RepoUrl.validate("https://"))
    }
}
