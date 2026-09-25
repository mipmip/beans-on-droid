package io.github.mipmip.beansondroid.bean

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BeanParserTest {

    private val parser = BeanParser()

    private fun parsed(result: ParseResult): Bean {
        assertTrue("expected Parsed but got $result", result is ParseResult.Parsed)
        return (result as ParseResult.Parsed).bean
    }

    @Test
    fun everyRealFixtureParses() {
        val results = Fixtures.real().map { parser.parseFile(it) }
        assertEquals(6, results.size)
        results.forEach { assertTrue("$it", it is ParseResult.Parsed) }
    }

    @Test
    fun realFixtureFieldsAreRead() {
        val bean = parsed(parser.parseFile(Fixtures.realNamed("beans-5ucr")))
        assertEquals("beans-5ucr", bean.id)
        assertEquals("investigate-warp-integration", bean.slug)
        assertEquals("Investigate Warp integration", bean.title)
        assertEquals("todo", bean.status)
        assertEquals("task", bean.type)
        assertEquals("normal", bean.priority)
        assertEquals(listOf("integrations"), bean.tags)
        assertEquals("Vy", bean.order)
        assertEquals(Instant.parse("2025-12-14T18:42:33Z"), bean.createdAt)
        assertEquals(Instant.parse("2026-03-07T23:11:57Z"), bean.updatedAt)
        assertTrue(bean.extras.isEmpty())
    }

    @Test
    fun blockedByIsRead() {
        val bean = parsed(parser.parseFile(Fixtures.realNamed("beans-l4ag")))
        assertEquals(listOf("beans-bbjk"), bean.blockedBy)
        assertTrue(bean.blocking.isEmpty())
    }

    @Test
    fun parentIsRead() {
        val withParent = Fixtures.real()
            .map { parsed(parser.parseFile(it)) }
            .filter { it.parent.isNotEmpty() }
        assertTrue("expected at least one fixture with a parent", withParent.isNotEmpty())
    }

    @Test
    fun archivedFlagIsSetOnlyWhenAsked() {
        val file = Fixtures.realNamed("beans-03uj")
        assertTrue(parsed(parser.parseFile(file, archived = true)).archived)
        assertFalse(parsed(parser.parseFile(file)).archived)
    }

    @Test
    fun bodyIsEverythingAfterTheFrontmatter() {
        val bean = parsed(parser.parseFile(Fixtures.realNamed("beans-03uj")))
        assertTrue(bean.body.startsWith("Add setup command"))
        assertTrue(bean.body.contains("## Summary of Changes"))
    }

    @Test
    fun frontmatterCommentIsNotTheId() {
        val content = """
            ---
            # beans-zzzz
            title: Comment is not the id
            status: todo
            ---

            Body.
        """.trimIndent()
        val bean = parsed(parser.parse("beans-aaaa--x.md", content))
        assertEquals("beans-aaaa", bean.id)
        assertEquals("Comment is not the id", bean.title)
    }

    @Test
    fun unknownFieldsAreKept() {
        val bean = parsed(parser.parseFile(Fixtures.synthetic("beans-unkn--unknown-fields.md")))
        assertEquals("someone", bean.extras["assignee"])
        assertEquals("3", bean.extras["estimate"])
        assertTrue(bean.extras.getValue("custom_block").contains("nested: value"))
        assertEquals("Bean carrying fields this app has never seen", bean.title)
        assertEquals("high", bean.priority)
    }

    @Test
    fun emptyBodyParses() {
        val bean = parsed(parser.parseFile(Fixtures.synthetic("beans-empt--empty-body.md")))
        assertEquals("", bean.body)
        assertEquals("Bean with no body at all", bean.title)
    }

    @Test
    fun malformedFrontmatterIsSkipped() {
        val result = parser.parseFile(Fixtures.synthetic("beans-mlfm--malformed-frontmatter.md"))
        assertTrue("$result", result is ParseResult.Skipped)
        val skipped = result as ParseResult.Skipped
        assertEquals("beans-mlfm--malformed-frontmatter.md", skipped.fileName)
        assertTrue(skipped.reason.isNotEmpty())
    }

    @Test
    fun fileWithoutFrontmatterIsSkipped() {
        val result = parser.parseFile(Fixtures.synthetic("beans-nofm--no-frontmatter.md"))
        assertTrue("$result", result is ParseResult.Skipped)
        assertEquals("no YAML frontmatter", (result as ParseResult.Skipped).reason)
    }

    @Test
    fun scalarIsAcceptedWhereListExpected() {
        val bean = parsed(
            parser.parseFile(Fixtures.synthetic("beans-scal--scalar-where-list-expected.md")),
        )
        assertEquals(listOf("single-tag"), bean.tags)
        assertEquals(listOf("beans-empt"), bean.blockedBy)
    }

    @Test
    fun filenameWithNoSeparatorParses() {
        val bean = parsed(parser.parseFile(Fixtures.synthetic("beansnoslug.md")))
        assertEquals("beansnoslug", bean.id)
        assertEquals("", bean.slug)
    }

    @Test
    fun bodyContainingDelimiterIsNotTruncated() {
        val content = """
            ---
            title: Body with a rule
            status: todo
            ---

            Before.

            ---

            After.
        """.trimIndent()
        val bean = parsed(parser.parse("beans-rule--x.md", content))
        assertTrue(bean.body.contains("Before."))
        assertTrue(bean.body.contains("After."))
    }

    @Test
    fun oneBadFileAmongManyDoesNotStopTheRest() {
        val files = Fixtures.real() + Fixtures.synthetic("beans-mlfm--malformed-frontmatter.md")
        val results = files.map { parser.parseFile(it) }
        assertEquals(6, results.count { it is ParseResult.Parsed })
        assertEquals(1, results.count { it is ParseResult.Skipped })
    }

    @Test
    fun allFieldsAreOptional() {
        val bean = parsed(parser.parse("beans-bare--x.md", "---\n---\n"))
        assertEquals("beans-bare", bean.id)
        assertEquals("", bean.title)
        assertEquals("", bean.status)
        assertTrue(bean.tags.isEmpty())
        assertEquals(null, bean.createdAt)
    }

    @Test
    fun unparseableTimestampLandsInExtras() {
        val content = """
            ---
            title: Bad timestamp
            created_at: not-a-date
            ---
        """.trimIndent()
        val bean = parsed(parser.parse("beans-time--x.md", content))
        assertEquals(null, bean.createdAt)
        assertEquals("not-a-date", bean.extras["created_at"])
    }

    @Test
    fun everyTypedFieldIsRead() {
        val content = """
            ---
            title: Everything
            status: in-progress
            type: epic
            priority: critical
            tags:
                - one
                - two
            created_at: 2026-01-02T03:04:05Z
            updated_at: 2026-01-03T03:04:05Z
            order: zzzV
            parent: beans-par1
            blocking:
                - beans-blk1
            blocked_by:
                - beans-blk2
                - beans-blk3
            ---

            Body text.
        """.trimIndent()
        val bean = parsed(parser.parse("beans-all--everything.md", content))
        assertEquals("Everything", bean.title)
        assertEquals("in-progress", bean.status)
        assertEquals("epic", bean.type)
        assertEquals("critical", bean.priority)
        assertEquals(listOf("one", "two"), bean.tags)
        assertNotNull(bean.createdAt)
        assertNotNull(bean.updatedAt)
        assertEquals("zzzV", bean.order)
        assertEquals("beans-par1", bean.parent)
        assertEquals(listOf("beans-blk1"), bean.blocking)
        assertEquals(listOf("beans-blk2", "beans-blk3"), bean.blockedBy)
        assertEquals("Body text.", bean.body)
    }
}
