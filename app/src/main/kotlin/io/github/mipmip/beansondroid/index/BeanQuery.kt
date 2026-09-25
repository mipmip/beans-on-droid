package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean

data class BeanQuery(
    val statuses: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val term: String = "",
    val includeArchived: Boolean = false,
) {
    val isEmpty: Boolean
        get() = statuses.isEmpty() && types.isEmpty() && tags.isEmpty() && term.isBlank()
}

data class BeanRelations(
    val parent: Bean? = null,
    val children: List<Bean> = emptyList(),
    val blocking: List<Bean> = emptyList(),
    val blockedBy: List<Bean> = emptyList(),
    val unresolved: List<String> = emptyList(),
)

data class BeanFacets(
    val statuses: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)
