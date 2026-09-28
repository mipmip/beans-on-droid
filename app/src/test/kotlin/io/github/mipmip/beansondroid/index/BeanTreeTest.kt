package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeanTreeTest {

    private fun bean(
        id: String,
        parent: String = "",
        status: String = "todo",
        title: String = id,
        type: String = "task",
    ) = Bean(id = id, parent = parent, status = status, title = title, type = type)

    private fun rows(tree: BeanTree) = tree.rows.map { "${"  ".repeat(it.depth)}${it.bean.id}" }

    @Test
    fun aChildIsNestedUnderItsParent() {
        val index = BeanIndex(listOf(bean("p"), bean("c1", parent = "p"), bean("c2", parent = "p")))
        val tree = index.tree(BeanQuery())
        assertEquals(listOf("p", "  c1", "  c2"), rows(tree))
        assertEquals(3, tree.matchCount)
        assertEquals(0, tree.contextCount)
    }

    @Test
    fun beansWithoutAParentSitAtTheTopLevel() {
        val index = BeanIndex(listOf(bean("p"), bean("c", parent = "p"), bean("loose")))
        val tree = index.tree(BeanQuery())
        assertEquals(listOf("loose", "p", "  c"), rows(tree))
        assertTrue(tree.rows.none { it.isContext })
    }

    @Test
    fun orderingAppliesWithinEachLevel() {
        val index = BeanIndex(
            listOf(
                bean("p"),
                bean("later", parent = "p", status = "todo"),
                bean("first", parent = "p", status = "in-progress"),
            ),
        )
        assertEquals(listOf("p", "  first", "  later"), rows(index.tree(BeanQuery())))
    }

    @Test
    fun deeperNesting() {
        val index = BeanIndex(
            listOf(bean("a"), bean("b", parent = "a"), bean("c", parent = "b")),
        )
        assertEquals(listOf("a", "  b", "    c"), rows(index.tree(BeanQuery())))
    }

    @Test
    fun aCycleDoesNotHangAndKeepsBothBeans() {
        val index = BeanIndex(listOf(bean("a", parent = "b"), bean("b", parent = "a")))
        val tree = index.tree(BeanQuery())
        assertEquals(setOf("a", "b"), tree.rows.map { it.bean.id }.toSet())
        assertEquals(2, tree.rows.size)
        assertEquals(2, tree.matchCount)
    }

    @Test
    fun aLongerCycleDoesNotHang() {
        val index = BeanIndex(
            listOf(bean("a", parent = "c"), bean("b", parent = "a"), bean("c", parent = "b")),
        )
        val tree = index.tree(BeanQuery())
        assertEquals(3, tree.rows.size)
        assertEquals(setOf("a", "b", "c"), tree.rows.map { it.bean.id }.toSet())
    }

    @Test
    fun aParentNotInTheRepositoryLeavesTheBeanAtTheTopLevel() {
        val index = BeanIndex(listOf(bean("orphan", parent = "ghost")))
        val tree = index.tree(BeanQuery())
        assertEquals(listOf("orphan"), rows(tree))
        assertEquals(0, tree.contextCount)
    }

    @Test
    fun aFilteredOutParentBecomesContext() {
        val index = BeanIndex(
            listOf(
                bean("p", status = "in-progress"),
                bean("c", parent = "p", status = "todo"),
            ),
        )
        val tree = index.tree(BeanQuery(statuses = setOf("todo")))
        assertEquals(listOf("p", "  c"), rows(tree))
        assertTrue(tree.rows.first { it.bean.id == "p" }.isContext)
        assertFalse(tree.rows.first { it.bean.id == "c" }.isContext)
    }

    @Test
    fun contextIsNotCounted() {
        val beans = (1..8).map { bean("x$it") } +
            listOf(bean("p", status = "in-progress"), bean("c", parent = "p", status = "todo"))
        val index = BeanIndex(beans)
        val tree = index.tree(BeanQuery(statuses = setOf("todo")))
        assertEquals(9, tree.matchCount)
        assertEquals(1, tree.contextCount)
        assertEquals(10, tree.rows.size)
    }

    @Test
    fun noChildrenMatchingMeansNoRowsAtAll() {
        val index = BeanIndex(
            listOf(
                bean("p", status = "in-progress"),
                bean("c", parent = "p", status = "in-progress"),
                bean("other", status = "todo"),
            ),
        )
        val tree = index.tree(BeanQuery(statuses = setOf("todo")))
        assertEquals(listOf("other"), rows(tree))
    }

    @Test
    fun anEmptyResultIsAnEmptyTree() {
        val index = BeanIndex(listOf(bean("a")))
        val tree = index.tree(BeanQuery(term = "nothing matches this"))
        assertEquals(0, tree.rows.size)
        assertEquals(0, tree.matchCount)
    }

    @Test
    fun everyMatchedBeanAppearsExactlyOnce() {
        val index = BeanIndex(
            listOf(
                bean("p"),
                bean("c1", parent = "p"),
                bean("c2", parent = "p"),
                bean("g", parent = "c1"),
                bean("loose"),
            ),
        )
        val tree = index.tree(BeanQuery())
        val ids = tree.rows.map { it.bean.id }
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(5, ids.size)
    }

    @Test
    fun rowsNestUnderTheDefaultOrder() {
        val index = BeanIndex(listOf(bean("p"), bean("c", parent = "p")))
        val tree = index.rows(BeanQuery())
        assertEquals(listOf(0, 1), tree.rows.map { it.depth })
    }

    @Test
    fun anExplicitSortFlattens() {
        val index = BeanIndex(listOf(bean("p"), bean("c", parent = "p")))
        val tree = index.rows(BeanQuery(sort = BeanSort.Title))
        assertEquals(listOf(0, 0), tree.rows.map { it.depth })
        assertTrue(tree.rows.none { it.isContext })
    }

    @Test
    fun aSearchTermFlattens() {
        val index = BeanIndex(listOf(bean("p", title = "parent bean"), bean("c", parent = "p", title = "child bean")))
        val tree = index.rows(BeanQuery(term = "bean"))
        assertEquals(listOf(0, 0), tree.rows.map { it.depth })
    }

    @Test
    fun clearingTheSearchRestoresTheTree() {
        val index = BeanIndex(
            listOf(bean("p", title = "parent bean"), bean("c", parent = "p", title = "child bean")),
        )
        assertEquals(listOf(0, 0), index.rows(BeanQuery(term = "bean")).rows.map { it.depth })
        assertEquals(listOf(0, 1), index.rows(BeanQuery(term = "")).rows.map { it.depth })
    }

    @Test
    fun aReversedDefaultOrderAlsoFlattens() {
        val index = BeanIndex(listOf(bean("p"), bean("c", parent = "p")))
        val tree = index.rows(BeanQuery(direction = SortDirection.Descending))
        assertEquals(listOf(0, 0), tree.rows.map { it.depth })
    }

    @Test
    fun theRealFixturesProduceAShallowForest() {
        val beans = io.github.mipmip.beansondroid.bean.Fixtures.real()
            .map { io.github.mipmip.beansondroid.bean.BeanParser().parseFile(it) }
            .filterIsInstance<io.github.mipmip.beansondroid.bean.ParseResult.Parsed>()
            .map { it.bean }
        val tree = BeanIndex(beans).tree(BeanQuery())
        assertEquals(beans.size, tree.matchCount)
        assertEquals(beans.size, tree.rows.count { !it.isContext })
    }
}
