package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean

data class BeanRow(
    val bean: Bean,
    val depth: Int,
    val isContext: Boolean = false,
)

data class BeanTree(
    val rows: List<BeanRow> = emptyList(),
    val matchCount: Int = 0,
) {
    val contextCount: Int get() = rows.count { it.isContext }
}

internal object BeanTreeBuilder {

    fun build(
        matched: List<Bean>,
        byId: (String) -> Bean?,
        ordering: Comparator<Bean>,
    ): BeanTree {
        if (matched.isEmpty()) return BeanTree()

        val matchedIds = matched.mapTo(mutableSetOf()) { it.id }
        val context = linkedMapOf<String, Bean>()
        val childrenOf = mutableMapOf<String, MutableList<Bean>>()
        val roots = mutableListOf<Bean>()

        for (bean in matched) {
            val parentId = bean.parent
            when {
                parentId.isEmpty() -> roots += bean

                parentId in matchedIds ->
                    childrenOf.getOrPut(parentId) { mutableListOf() } += bean

                else -> {
                    val parent = byId(parentId)
                    if (parent == null) {
                        roots += bean
                    } else {
                        context[parentId] = parent
                        childrenOf.getOrPut(parentId) { mutableListOf() } += bean
                    }
                }
            }
        }

        val rows = mutableListOf<BeanRow>()
        val visited = mutableSetOf<String>()

        fun emit(bean: Bean, depth: Int, isContext: Boolean) {
            if (!visited.add(bean.id)) return
            rows += BeanRow(bean, depth, isContext)
            childrenOf[bean.id]
                ?.sortedWith(ordering)
                ?.forEach { emit(it, depth + 1, isContext = false) }
        }

        val topLevel = (roots + context.values).sortedWith(ordering)
        for (bean in topLevel) {
            emit(bean, 0, isContext = bean.id in context && bean.id !in matchedIds)
        }

        // A parent cycle leaves beans unreachable from any root. Promote them so
        // the result is finite and still contains everything that matched.
        for (bean in matched.sortedWith(ordering)) {
            emit(bean, 0, isContext = false)
        }

        return BeanTree(rows = rows, matchCount = matched.size)
    }
}
