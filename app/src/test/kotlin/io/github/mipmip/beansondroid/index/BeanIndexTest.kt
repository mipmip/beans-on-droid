package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.bean.BeanParser
import io.github.mipmip.beansondroid.bean.Fixtures
import io.github.mipmip.beansondroid.bean.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BeanIndexTest {

    private fun bean(
        id: String,
        title: String = id,
        status: String = "todo",
        type: String = "task",
        tags: List<String> = emptyList(),
        order: String = "",
        parent: String = "",
        blocking: List<String> = emptyList(),
        blockedBy: List<String> = emptyList(),
        body: String = "",
        archived: Boolean = false,
    ) = Bean(
        id = id,
        title = title,
        status = status,
        type = type,
        tags = tags,
        order = order,
        parent = parent,
        blocking = blocking,
        blockedBy = blockedBy,
        body = body,
        archived = archived,
    )

    private val sample = BeanIndex(
        listOf(
            bean("b-1", title = "Write the parser", status = "todo", type = "task", tags = listOf("core"), order = "a"),
            bean("b-2", title = "Ship the index", status = "in-progress", type = "epic", tags = listOf("core", "ui"), order = "b"),
            bean("b-3", title = "Polish the theme", status = "todo", type = "epic", body = "Colours and shapes"),
            bean("b-4", title = "Old work", status = "completed", type = "task", archived = true),
        ),
    )

    @Test
    fun emptyQueryReturnsEveryActiveBean() {
        val result = sample.query(BeanQuery())
        assertEquals(listOf("b-1", "b-2", "b-3"), result.map { it.id })
    }

    @Test
    fun singleStatusFilter() {
        val result = sample.query(BeanQuery(statuses = setOf("todo")))
        assertEquals(listOf("b-1", "b-3"), result.map { it.id })
    }

    @Test
    fun severalValuesWithinOneFacet() {
        val result = sample.query(BeanQuery(statuses = setOf("todo", "in-progress")))
        assertEquals(listOf("b-1", "b-2", "b-3"), result.map { it.id })
    }

    @Test
    fun differentFacetsCombine() {
        val result = sample.query(BeanQuery(statuses = setOf("todo"), types = setOf("epic")))
        assertEquals(listOf("b-3"), result.map { it.id })
    }

    @Test
    fun tagFilter() {
        val result = sample.query(BeanQuery(tags = setOf("ui")))
        assertEquals(listOf("b-2"), result.map { it.id })
    }

    @Test
    fun searchMatchesTitleIgnoringCase() {
        val result = sample.query(BeanQuery(term = "PARSER"))
        assertEquals(listOf("b-1"), result.map { it.id })
    }

    @Test
    fun searchMatchesBody() {
        val result = sample.query(BeanQuery(term = "colours"))
        assertEquals(listOf("b-3"), result.map { it.id })
    }

    @Test
    fun searchMatchesId() {
        val result = sample.query(BeanQuery(term = "b-2"))
        assertEquals(listOf("b-2"), result.map { it.id })
    }

    @Test
    fun searchCombinesWithFilter() {
        val result = sample.query(BeanQuery(term = "the", statuses = setOf("in-progress")))
        assertEquals(listOf("b-2"), result.map { it.id })
    }

    @Test
    fun searchWithNoMatchReturnsNothing() {
        assertTrue(sample.query(BeanQuery(term = "kangaroo")).isEmpty())
    }

    @Test
    fun archivedExcludedByDefault() {
        assertTrue(sample.query(BeanQuery()).none { it.archived })
    }

    @Test
    fun archivedIncludedOnRequest() {
        val result = sample.query(BeanQuery(includeArchived = true))
        assertEquals(listOf("b-1", "b-2", "b-4", "b-3"), result.map { it.id })
    }

    @Test
    fun orderedBeansComeFirstInOrderValue() {
        val index = BeanIndex(listOf(bean("z", order = "zzzV"), bean("v", order = "Vy")))
        assertEquals(listOf("v", "z"), index.all.map { it.id })
    }

    @Test
    fun beansWithoutOrderComeLast() {
        val index = BeanIndex(listOf(bean("none"), bean("some", order = "m")))
        assertEquals(listOf("some", "none"), index.all.map { it.id })
    }

    @Test
    fun titleBreaksTiesOnOrder() {
        val index = BeanIndex(
            listOf(
                bean("x", title = "Beta", order = "m"),
                bean("y", title = "Alpha", order = "m"),
            ),
        )
        assertEquals(listOf("y", "x"), index.all.map { it.id })
    }

    @Test
    fun parentAndChildrenResolve() {
        val index = BeanIndex(listOf(bean("a"), bean("b", parent = "a"), bean("c", parent = "a")))
        assertEquals(listOf("b", "c"), index.relations("a").children.map { it.id })
        assertEquals("a", index.relations("b").parent?.id)
    }

    @Test
    fun inverseBlockingIsDerived() {
        val index = BeanIndex(listOf(bean("a", blocking = listOf("b")), bean("b")))
        assertEquals(listOf("b"), index.relations("a").blocking.map { it.id })
        assertEquals(listOf("a"), index.relations("b").blockedBy.map { it.id })
    }

    @Test
    fun mirroredDeclarationIsNotDuplicated() {
        val index = BeanIndex(
            listOf(bean("a", blocking = listOf("b")), bean("b", blockedBy = listOf("a"))),
        )
        assertEquals(listOf("a"), index.relations("b").blockedBy.map { it.id })
        assertEquals(listOf("b"), index.relations("a").blocking.map { it.id })
    }

    @Test
    fun unresolvedRelationshipIsReported() {
        val index = BeanIndex(listOf(bean("a", parent = "ghost", blocking = listOf("phantom"))))
        val relations = index.relations("a")
        assertNull(relations.parent)
        assertTrue(relations.blocking.isEmpty())
        assertEquals(listOf("ghost", "phantom"), relations.unresolved)
    }

    @Test
    fun relationsForUnknownBeanAreEmpty() {
        assertEquals(BeanRelations(), sample.relations("nope"))
    }

    @Test
    fun facetsReflectTheData() {
        assertEquals(listOf("completed", "in-progress", "todo"), sample.facets.statuses)
        assertEquals(listOf("epic", "task"), sample.facets.types)
        assertEquals(listOf("core", "ui"), sample.facets.tags)
    }

    @Test
    fun byIdFindsAndMisses() {
        assertEquals("b-1", sample.byId("b-1")?.id)
        assertNull(sample.byId("absent"))
        assertEquals(4, sample.size)
    }

    @Test
    fun indexBuiltFromRealFixtures() {
        val beans = Fixtures.real()
            .map { BeanParser().parseFile(it) }
            .filterIsInstance<ParseResult.Parsed>()
            .map { it.bean }
        val index = BeanIndex(beans)
        assertEquals(6, index.size)
        assertTrue(index.facets.statuses.isNotEmpty())
        assertTrue(index.query(BeanQuery(term = "worktree")).isNotEmpty())
    }
}
