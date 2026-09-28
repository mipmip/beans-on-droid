package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The beans tool's own ordering, used as the answer. If this fails, this app
 * and the tool disagree about how a repository should be listed.
 */
class BeanOrderingOracleTest {

    private fun oracle(): List<Bean> {
        val stream = requireNotNull(
            javaClass.classLoader?.getResourceAsStream("fixtures/ordering/beans-list-order.tsv"),
        ) { "ordering fixture missing" }

        return stream.bufferedReader().readLines()
            .filter { it.isNotBlank() }
            .map { line ->
                val f = line.split('\t')
                Bean(
                    id = f[0],
                    status = f[1],
                    order = f[2],
                    priority = f[3],
                    type = f[4],
                    title = f.getOrElse(5) { "" },
                )
            }
    }

    @Test
    fun theFixtureIsSubstantial() {
        val beans = oracle()
        assertEquals(254, beans.size)
        assertTrue(beans.any { it.order.isNotEmpty() })
        assertTrue(beans.any { it.priority != "normal" })
        assertTrue(beans.map { it.status }.distinct().size >= 3)
    }

    @Test
    fun ourComparatorReproducesTheToolsOrder() {
        val fromTool = oracle()
        val ourSort = fromTool.shuffled(java.util.Random(20260928)).sortedWith(BeanIndex.ORDERING)

        val expected = fromTool.map { it.id }
        val actual = ourSort.map { it.id }

        if (expected != actual) {
            val firstDivergence = expected.indices.first { expected[it] != actual[it] }
            val window = (firstDivergence - 2).coerceAtLeast(0)..(firstDivergence + 2)
                .coerceAtMost(expected.lastIndex)
            val detail = window.joinToString("\n") { i ->
                val e = fromTool[i]
                val a = ourSort[i]
                "  [$i] tool=${e.id} ${e.status}/${e.priority}/${e.type} " +
                    "| ours=${a.id} ${a.status}/${a.priority}/${a.type}"
            }
            throw AssertionError("diverges at index $firstDivergence\n$detail")
        }
        assertEquals(expected, actual)
    }
}
