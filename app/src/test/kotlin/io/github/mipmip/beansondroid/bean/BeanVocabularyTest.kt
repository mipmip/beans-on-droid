package io.github.mipmip.beansondroid.bean

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BeanVocabularyTest {

    @Test
    fun onlyCompletedAndScrappedAreClosed() {
        val closed = BeanVocabulary.statuses.filter { BeanVocabulary.isClosed(it) }
        assertEquals(listOf("completed", "scrapped"), closed)
    }

    @Test
    fun everyListedStatusIsAnswered() {
        BeanVocabulary.statuses.forEach { status ->
            val expected = status == "completed" || status == "scrapped"
            assertEquals(status, expected, BeanVocabulary.isClosed(status))
        }
    }

    @Test
    fun anAbsentOrUnknownStatusIsNotClosed() {
        assertFalse(BeanVocabulary.isClosed(""))
        assertFalse(BeanVocabulary.isClosed("archived"))
        assertFalse(BeanVocabulary.isClosed("cancelled"))
    }
}
