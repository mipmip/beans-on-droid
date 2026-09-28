package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import org.junit.Assert.assertEquals
import org.junit.Test

class BeanOrderingTest {

    private fun bean(
        id: String,
        status: String = "todo",
        order: String = "",
        priority: String = "",
        type: String = "task",
        title: String = id,
    ) = Bean(id = id, status = status, order = order, priority = priority, type = type, title = title)

    private fun ordered(vararg beans: Bean) = beans.sortedWith(BeanIndex.ORDERING).map { it.id }

    @Test
    fun statusIsThePrimaryKey() {
        val result = ordered(
            bean("todo", status = "todo", order = "a", priority = "critical"),
            bean("prog", status = "in-progress", order = "z", priority = "deferred"),
        )
        assertEquals(listOf("prog", "todo"), result)
    }

    @Test
    fun everyConfiguredStatusInOrder() {
        val result = ordered(
            bean("e", status = "scrapped"),
            bean("d", status = "completed"),
            bean("c", status = "draft"),
            bean("b", status = "todo"),
            bean("a", status = "in-progress"),
        )
        assertEquals(listOf("a", "b", "c", "d", "e"), result)
    }

    @Test
    fun orderedBeans() {
        assertEquals(
            listOf("v", "z"),
            ordered(bean("z", order = "zzzV"), bean("v", order = "Vy")),
        )
    }

    @Test
    fun missingOrder() {
        assertEquals(
            listOf("has", "none"),
            ordered(bean("none"), bean("has", order = "m")),
        )
    }

    @Test
    fun tieOnOrderFallsThroughToPriorityThenTitle() {
        assertEquals(
            listOf("high", "low"),
            ordered(
                bean("low", order = "m", priority = "low"),
                bean("high", order = "m", priority = "high"),
            ),
        )
        assertEquals(
            listOf("alpha", "beta"),
            ordered(
                bean("beta", order = "m", priority = "normal", title = "Beta"),
                bean("alpha", order = "m", priority = "normal", title = "Alpha"),
            ),
        )
    }

    @Test
    fun priorityOrdersBeansWithoutAnOrder() {
        assertEquals(
            listOf("crit", "high", "norm", "low", "def"),
            ordered(
                bean("def", priority = "deferred"),
                bean("low", priority = "low"),
                bean("norm", priority = "normal"),
                bean("high", priority = "high"),
                bean("crit", priority = "critical"),
            ),
        )
    }

    @Test
    fun absentPriorityCountsAsNormal() {
        assertEquals(
            listOf("absent", "low"),
            ordered(bean("low", priority = "low"), bean("absent", priority = "")),
        )
        assertEquals(
            listOf("high", "absent"),
            ordered(bean("absent", priority = ""), bean("high", priority = "high")),
        )
    }

    @Test
    fun typeBreaksAPriorityTie() {
        assertEquals(
            listOf("m", "e", "b", "f", "t"),
            ordered(
                bean("t", type = "task"),
                bean("f", type = "feature"),
                bean("b", type = "bug"),
                bean("e", type = "epic"),
                bean("m", type = "milestone"),
            ),
        )
    }

    @Test
    fun titleBreaksATypeTieIgnoringCase() {
        assertEquals(
            listOf("a", "b"),
            ordered(bean("b", title = "Banana"), bean("a", title = "apple")),
        )
    }

    @Test
    fun anUnrecognisedStatusSortsLastAndSurvives() {
        val result = ordered(
            bean("weird", status = "marinating"),
            bean("scrapped", status = "scrapped"),
            bean("prog", status = "in-progress"),
        )
        assertEquals(listOf("prog", "scrapped", "weird"), result)
    }

    @Test
    fun anUnrecognisedPriorityAndTypeSortLastAndSurvive() {
        assertEquals(
            listOf("known", "weird"),
            ordered(bean("weird", priority = "urgent-ish"), bean("known", priority = "deferred")),
        )
        assertEquals(
            listOf("task", "weird"),
            ordered(bean("weird", type = "chore"), bean("task", type = "task")),
        )
    }

    @Test
    fun idBreaksAFullTie() {
        assertEquals(
            listOf("a", "b"),
            ordered(bean("b", title = "same"), bean("a", title = "same")),
        )
    }
}
