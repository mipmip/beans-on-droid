package io.github.mipmip.beansondroid.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BeansLayoutTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun configuredPathIsUsed() {
        val root = temp.newFolder("repo")
        root.resolve(".beans.yml").writeText("beans:\n    path: issues\n")
        root.resolve("issues").mkdirs()

        val result = BeansLayout.beanDirectory(root)
        assertEquals(root.resolve("issues"), result.valueOrNull())
        assertEquals("issues", BeansLayout.configuredBeanPath(root))
    }

    @Test
    fun defaultPathIsUsedWithoutConfig() {
        val root = temp.newFolder("repo")
        root.resolve(".beans").mkdirs()

        assertEquals(root.resolve(".beans"), BeansLayout.beanDirectory(root).valueOrNull())
        assertNull(BeansLayout.configuredBeanPath(root))
    }

    @Test
    fun missingBeanDirectoryIsNotABeansRepository() {
        val root = temp.newFolder("repo")
        val error = BeansLayout.beanDirectory(root).errorOrNull()
        assertTrue("$error", error is RepoError.NotABeansRepository)
    }

    @Test
    fun configuredPathThatIsNotADirectoryFails() {
        val root = temp.newFolder("repo")
        root.resolve(".beans.yml").writeText("beans:\n    path: nowhere\n")
        val error = BeansLayout.beanDirectory(root).errorOrNull()
        assertTrue("$error", error is RepoError.NotABeansRepository)
    }

    @Test
    fun unreadableConfigFallsBackToDefault() {
        val root = temp.newFolder("repo")
        root.resolve(".beans.yml").writeText("beans: [unclosed\n")
        root.resolve(".beans").mkdirs()
        assertEquals(root.resolve(".beans"), BeansLayout.beanDirectory(root).valueOrNull())
    }

    @Test
    fun beanFilesIncludeArchiveAndSkipOtherFiles() {
        val root = temp.newFolder("repo")
        val beans = root.resolve(".beans").apply { mkdirs() }
        beans.resolve("a--one.md").writeText("---\ntitle: a\n---\n")
        beans.resolve("notes.txt").writeText("ignore me")
        beans.resolve("archive").apply { mkdirs() }.resolve("b--two.md").writeText("---\ntitle: b\n---\n")

        val files = BeansLayout.beanFiles(beans)
        assertEquals(listOf("a--one.md" to false, "b--two.md" to true), files.map { it.first.name to it.second })
    }
}
