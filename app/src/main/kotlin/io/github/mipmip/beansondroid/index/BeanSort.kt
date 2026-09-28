package io.github.mipmip.beansondroid.index

import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.bean.BeanVocabulary
import kotlinx.serialization.Serializable

@Serializable
enum class BeanSort {
    Default,
    Created,
    Updated,
    Status,
    Priority,
    Type,
    Title,
    Id,
    ;

    val defaultDirection: SortDirection
        get() = when (this) {
            Created, Updated -> SortDirection.Descending
            else -> SortDirection.Ascending
        }
}

@Serializable
enum class SortDirection {
    Ascending,
    Descending,
    ;

    fun reversed(): SortDirection = when (this) {
        Ascending -> Descending
        Descending -> Ascending
    }
}

internal object BeanSorting {

    fun comparator(sort: BeanSort, direction: SortDirection): Comparator<Bean> {
        if (sort == BeanSort.Default) return BeanIndex.ORDERING

        val byField = fieldComparator(sort, direction)
        return Comparator { a, b ->
            val aMissing = isMissing(sort, a)
            val bMissing = isMissing(sort, b)
            when {
                aMissing != bMissing -> if (aMissing) 1 else -1
                else -> byField.compare(a, b).takeIf { it != 0 } ?: a.id.compareTo(b.id)
            }
        }
    }

    private fun fieldComparator(sort: BeanSort, direction: SortDirection): Comparator<Bean> {
        val ascending: Comparator<Bean> = when (sort) {
            BeanSort.Created -> compareBy { it.createdAt }
            BeanSort.Updated -> compareBy { it.updatedAt }
            BeanSort.Status -> compareBy { BeanVocabulary.statusRank(it.status) }
            BeanSort.Priority -> compareBy { BeanVocabulary.priorityRank(it.priority) }
            BeanSort.Type -> compareBy { BeanVocabulary.typeRank(it.type) }
            BeanSort.Title -> compareBy { it.title.lowercase() }
            BeanSort.Id -> compareBy { it.id }
            BeanSort.Default -> BeanIndex.ORDERING
        }
        return when (direction) {
            SortDirection.Ascending -> ascending
            SortDirection.Descending -> ascending.reversed()
        }
    }

    private fun isMissing(sort: BeanSort, bean: Bean): Boolean = when (sort) {
        BeanSort.Created -> bean.createdAt == null
        BeanSort.Updated -> bean.updatedAt == null
        BeanSort.Title -> bean.title.isEmpty()
        else -> false
    }
}
