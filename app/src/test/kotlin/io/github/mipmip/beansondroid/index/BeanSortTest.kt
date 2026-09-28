package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class BeanSortTest {

    private fun bean(
        id: String,
        title: String = id,
        status: String = "todo",
        type: String = "task",
        priority: String = "normal",
        created: String? = null,
        updated: String? = null,
    ) = Bean(
        id = id,
        title = title,
        status = status,
        type = type,
        priority = priority,
        createdAt = created?.let(Instant::parse),
        updatedAt = updated?.let(Instant::parse),
    )

    private val index = BeanIndex(
        listOf(
            bean("c", title = "Cherry", status = "draft", type = "bug", priority = "low", created = "2026-01-03T00:00:00Z", updated = "2026-03-01T00:00:00Z"),
            bean("a", title = "Apple", status = "todo", type = "epic", priority = "high", created = "2026-01-01T00:00:00Z", updated = "2026-02-01T00:00:00Z"),
            bean("b", title = "banana", status = "in-progress", type = "feature", priority = "critical", created = "2026-01-02T00:00:00Z", updated = "2026-01-15T00:00:00Z"),
        ),
    )

    private fun ids(sort: BeanSort, direction: SortDirection? = null) =
        index.query(BeanQuery(sort = sort, direction = direction)).map { it.id }

    @Test
    fun defaultSortUsesTheDefaultComparator() {
        assertEquals(listOf("b", "a", "c"), ids(BeanSort.Default))
    }

    @Test
    fun updatedIsNewestFirstByDefault() {
        assertEquals(listOf("c", "a", "b"), ids(BeanSort.Updated))
    }

    @Test
    fun createdIsNewestFirstByDefault() {
        assertEquals(listOf("c", "b", "a"), ids(BeanSort.Created))
    }

    @Test
    fun timeFieldsReverseToOldestFirst() {
        assertEquals(listOf("a", "b", "c"), ids(BeanSort.Created, SortDirection.Ascending))
    }

    @Test
    fun titleIsAscendingByDefaultAndIgnoresCase() {
        assertEquals(listOf("a", "b", "c"), ids(BeanSort.Title))
        assertEquals(listOf("c", "b", "a"), ids(BeanSort.Title, SortDirection.Descending))
    }

    @Test
    fun statusUsesItsConfiguredOrder() {
        assertEquals(listOf("b", "a", "c"), ids(BeanSort.Status))
        assertEquals(listOf("c", "a", "b"), ids(BeanSort.Status, SortDirection.Descending))
    }

    @Test
    fun priorityUsesItsConfiguredOrderNotAlphabetical() {
        assertEquals(listOf("b", "a", "c"), ids(BeanSort.Priority))
    }

    @Test
    fun typeUsesItsConfiguredOrder() {
        assertEquals(listOf("a", "c", "b"), ids(BeanSort.Type))
    }

    @Test
    fun idSortsLexicographically() {
        assertEquals(listOf("a", "b", "c"), ids(BeanSort.Id))
        assertEquals(listOf("c", "b", "a"), ids(BeanSort.Id, SortDirection.Descending))
    }

    @Test
    fun beansMissingTheFieldGoLastAscending() {
        val withGap = BeanIndex(
            listOf(
                bean("none"),
                bean("has", created = "2026-01-01T00:00:00Z"),
            ),
        )
        assertEquals(
            listOf("has", "none"),
            withGap.query(BeanQuery(sort = BeanSort.Created)).map { it.id },
        )
    }

    @Test
    fun beansMissingTheFieldStayLastDescending() {
        val withGap = BeanIndex(
            listOf(
                bean("none"),
                bean("old", created = "2026-01-01T00:00:00Z"),
                bean("new", created = "2026-06-01T00:00:00Z"),
            ),
        )
        assertEquals(
            listOf("new", "old", "none"),
            withGap.query(BeanQuery(sort = BeanSort.Created, direction = SortDirection.Descending))
                .map { it.id },
        )
        assertEquals(
            listOf("old", "new", "none"),
            withGap.query(BeanQuery(sort = BeanSort.Created, direction = SortDirection.Ascending))
                .map { it.id },
        )
    }

    @Test
    fun anUntitledBeanIsMissingForTitleSort() {
        val withGap = BeanIndex(listOf(bean("blank", title = ""), bean("named", title = "Named")))
        assertEquals(
            listOf("named", "blank"),
            withGap.query(BeanQuery(sort = BeanSort.Title)).map { it.id },
        )
    }

    @Test
    fun tiesBreakOnIdSoTheOrderIsStable() {
        val tied = BeanIndex(
            listOf(
                bean("zzz", updated = "2026-01-01T00:00:00Z"),
                bean("aaa", updated = "2026-01-01T00:00:00Z"),
                bean("mmm", updated = "2026-01-01T00:00:00Z"),
            ),
        )
        repeat(3) {
            assertEquals(
                listOf("aaa", "mmm", "zzz"),
                tied.query(BeanQuery(sort = BeanSort.Updated)).map { it.id },
            )
        }
    }

    @Test
    fun sortCombinesWithFiltersAndSearch() {
        val result = index.query(
            BeanQuery(statuses = setOf("todo", "draft"), sort = BeanSort.Title),
        )
        assertEquals(listOf("a", "c"), result.map { it.id })
    }

    @Test
    fun defaultDirectionsAreDeclared() {
        assertEquals(SortDirection.Descending, BeanSort.Created.defaultDirection)
        assertEquals(SortDirection.Descending, BeanSort.Updated.defaultDirection)
        assertEquals(SortDirection.Ascending, BeanSort.Title.defaultDirection)
        assertEquals(SortDirection.Ascending, BeanSort.Default.defaultDirection)
        assertEquals(SortDirection.Ascending, SortDirection.Descending.reversed())
    }
}
