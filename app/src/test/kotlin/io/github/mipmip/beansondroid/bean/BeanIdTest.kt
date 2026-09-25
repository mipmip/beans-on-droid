package io.github.mipmip.beansondroid.bean

import org.junit.Assert.assertEquals
import org.junit.Test

class BeanIdTest {

    @Test
    fun doubleDashSeparatesIdFromSlug() {
        val name = BeanId.parseFilename("beans-5ucr--investigate-warp-integration.md")
        assertEquals("beans-5ucr", name.id)
        assertEquals("investigate-warp-integration", name.slug)
    }

    @Test
    fun prefixContainingHyphensSurvives() {
        val name = BeanId.parseFilename("beans-on-droid-88wd--research-spike.md")
        assertEquals("beans-on-droid-88wd", name.id)
        assertEquals("research-spike", name.slug)
    }

    @Test
    fun dotFormIsSupported() {
        val name = BeanId.parseFilename("f7g.user-registration.md")
        assertEquals("f7g", name.id)
        assertEquals("user-registration", name.slug)
    }

    @Test
    fun legacySingleDashIsSupported() {
        val name = BeanId.parseFilename("f7g-user-registration.md")
        assertEquals("f7g", name.id)
        assertEquals("user-registration", name.slug)
    }

    @Test
    fun filenameWithNoSeparatorHasNoSlug() {
        val name = BeanId.parseFilename("beansnoslug.md")
        assertEquals("beansnoslug", name.id)
        assertEquals("", name.slug)
    }

    @Test
    fun doubleDashWinsOverDot() {
        val name = BeanId.parseFilename("a.b--c.d.md")
        assertEquals("a.b", name.id)
        assertEquals("c.d", name.slug)
    }
}
