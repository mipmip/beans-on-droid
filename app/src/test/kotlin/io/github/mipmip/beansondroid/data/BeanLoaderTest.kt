package io.github.mipmip.beansondroid.data

import io.github.mipmip.beansondroid.bean.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BeanLoaderTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun beanDirWith(files: List<File>, archived: List<File> = emptyList()): File {
        val dir = temp.newFolder("beans-${files.size}-${archived.size}-${System.nanoTime()}")
        files.forEach { it.copyTo(File(dir, it.name)) }
        if (archived.isNotEmpty()) {
            val archive = File(dir, "archive").apply { mkdirs() }
            archived.forEach { it.copyTo(File(archive, it.name)) }
        }
        return dir
    }

    @Test
    fun loadsEveryRealFixture() {
        val loaded = BeanLoader().load(beanDirWith(Fixtures.real()))
        assertEquals(6, loaded.index.size)
        assertTrue(loaded.skipped.isEmpty())
    }

    @Test
    fun reportsSkippedFilesWithoutLosingTheRest() {
        val dir = beanDirWith(
            Fixtures.real() + listOf(
                Fixtures.synthetic("beans-mlfm--malformed-frontmatter.md"),
                Fixtures.synthetic("beans-nofm--no-frontmatter.md"),
            ),
        )
        val loaded = BeanLoader().load(dir)
        assertEquals(6, loaded.index.size)
        assertEquals(2, loaded.skipped.size)
    }

    @Test
    fun marksArchivedBeans() {
        val all = Fixtures.real()
        val loaded = BeanLoader().load(beanDirWith(all.take(3), archived = all.drop(3)))
        assertEquals(6, loaded.index.size)
        assertEquals(3, loaded.index.all.count { it.archived })
    }

    @Test
    fun emptyDirectoryLoadsAnEmptyIndex() {
        val loaded = BeanLoader().load(temp.newFolder("empty"))
        assertEquals(0, loaded.index.size)
        assertTrue(loaded.skipped.isEmpty())
    }
}
