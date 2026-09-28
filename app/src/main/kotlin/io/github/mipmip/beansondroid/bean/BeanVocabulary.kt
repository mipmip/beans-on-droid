package io.github.mipmip.beansondroid.bean

object BeanVocabulary {

    val statuses: List<String> = listOf("in-progress", "todo", "draft", "completed", "scrapped")

    val types: List<String> = listOf("milestone", "epic", "bug", "feature", "task")

    val priorities: List<String> = listOf("critical", "high", "normal", "low", "deferred")

    private const val NORMAL = "normal"

    fun statusRank(status: String): Int = rank(statuses, status)

    fun typeRank(type: String): Int = rank(types, type)

    fun priorityRank(priority: String): Int = when {
        priority.isEmpty() -> priorities.indexOf(NORMAL)
        else -> rank(priorities, priority)
    }

    fun isRaised(priority: String): Boolean = priority == "critical" || priority == "high"

    fun isLowered(priority: String): Boolean = priority == "low" || priority == "deferred"

    private fun rank(known: List<String>, value: String): Int =
        known.indexOf(value).takeIf { it >= 0 } ?: known.size
}
