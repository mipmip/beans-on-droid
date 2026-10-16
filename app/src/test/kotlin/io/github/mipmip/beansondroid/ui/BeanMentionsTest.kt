package io.github.mipmip.beansondroid.ui

import io.github.mipmip.beansondroid.bean.Bean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeanMentionsTest {

    private val known = listOf(Bean(id = "beans-27wn"), Bean(id = "beans-bbjk"))

    private val lookup: (String) -> Bean? = { id -> known.firstOrNull { it.id == id } }

    private fun found(text: String): List<String> =
        BeanMentions.find(text, lookup).map { text.substring(it.start, it.end) }

    @Test
    fun aKnownIdIsFound() {
        assertEquals(listOf("beans-27wn"), found("See beans-27wn for the plan."))
    }

    @Test
    fun punctuationAroundTheIdDoesNotBlockIt() {
        assertEquals(listOf("beans-27wn"), found("Blocked (beans-27wn), again."))
        assertEquals(listOf("beans-27wn"), found("beans-27wn."))
    }

    @Test
    fun anIdFollowedByAnExtensionIsFound() {
        assertEquals(listOf("beans-27wn"), found("Open beans-27wn.md next."))
    }

    @Test
    fun theSameIdTwiceIsFoundTwice() {
        assertEquals(
            listOf("beans-27wn", "beans-bbjk", "beans-27wn"),
            found("beans-27wn and beans-bbjk, then beans-27wn again."),
        )
    }

    @Test
    fun aWordShapedLikeAnIdThatNoBeanHasIsNotMentioned() {
        assertTrue(found("This is in-progress, not a bean.").isEmpty())
    }

    @Test
    fun anIdInsideAPathIsLeftAlone() {
        assertTrue(found("The file is .beans/beans-27wn--auto-reload.md today.").isEmpty())
    }

    @Test
    fun aRunThatIsLongerThanAnIdIsNotAMention() {
        assertTrue(found("mybeans-27wnx is not an id.").isEmpty())
    }
}
