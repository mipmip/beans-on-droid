package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean

class BeanIndex(beans: List<Bean>) {

    val all: List<Bean> = beans.sortedWith(ORDERING)

    private val byId: Map<String, Bean> = all.associateBy { it.id }

    private val childrenOf: Map<String, List<Bean>> = all
        .filter { it.parent.isNotEmpty() }
        .groupBy { it.parent }
        .mapValues { (_, children) -> children.sortedWith(ORDERING) }

    private val blockingEdges: Map<String, Set<String>>
    private val blockedByEdges: Map<String, Set<String>>

    init {
        val blocking = mutableMapOf<String, MutableSet<String>>()
        val blockedBy = mutableMapOf<String, MutableSet<String>>()
        for (bean in all) {
            for (target in bean.blocking) {
                blocking.getOrPut(bean.id) { linkedSetOf() }.add(target)
                blockedBy.getOrPut(target) { linkedSetOf() }.add(bean.id)
            }
            for (blocker in bean.blockedBy) {
                blockedBy.getOrPut(bean.id) { linkedSetOf() }.add(blocker)
                blocking.getOrPut(blocker) { linkedSetOf() }.add(bean.id)
            }
        }
        blockingEdges = blocking
        blockedByEdges = blockedBy
    }

    val facets: BeanFacets = BeanFacets(
        statuses = all.mapNotNull { it.status.ifEmpty { null } }.distinct().sorted(),
        types = all.mapNotNull { it.type.ifEmpty { null } }.distinct().sorted(),
        tags = all.flatMap { it.tags }.distinct().sorted(),
    )

    val size: Int get() = all.size

    fun byId(id: String): Bean? = byId[id]

    fun query(query: BeanQuery): List<Bean> {
        val term = query.term.trim().lowercase()
        return all.filter { bean ->
            (query.includeArchived || !bean.archived) &&
                (query.statuses.isEmpty() || bean.status in query.statuses) &&
                (query.types.isEmpty() || bean.type in query.types) &&
                (query.tags.isEmpty() || bean.tags.any { it in query.tags }) &&
                (term.isEmpty() || matches(bean, term))
        }
    }

    fun relations(id: String): BeanRelations {
        val bean = byId[id] ?: return BeanRelations()

        val unresolved = linkedSetOf<String>()

        val parent = when {
            bean.parent.isEmpty() -> null
            else -> byId[bean.parent] ?: null.also { unresolved.add(bean.parent) }
        }

        val blocking = resolve(blockingEdges[id].orEmpty(), unresolved)
        val blockedBy = resolve(blockedByEdges[id].orEmpty(), unresolved)

        return BeanRelations(
            parent = parent,
            children = childrenOf[id].orEmpty(),
            blocking = blocking,
            blockedBy = blockedBy,
            unresolved = unresolved.toList(),
        )
    }

    private fun resolve(ids: Set<String>, unresolved: MutableSet<String>): List<Bean> =
        ids.mapNotNull { target ->
            byId[target] ?: null.also { unresolved.add(target) }
        }.sortedWith(ORDERING)

    private fun matches(bean: Bean, term: String): Boolean =
        bean.title.lowercase().contains(term) ||
            bean.body.lowercase().contains(term) ||
            bean.id.lowercase().contains(term)

    companion object {
        private val ORDERING = compareBy<Bean>(
            { it.order.isEmpty() },
            { it.order },
            { it.title },
            { it.id },
        )
    }
}
