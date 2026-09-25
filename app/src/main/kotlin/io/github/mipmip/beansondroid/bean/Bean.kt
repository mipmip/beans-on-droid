package io.github.mipmip.beansondroid.bean

import java.time.Instant

data class Bean(
    val id: String,
    val slug: String = "",
    val title: String = "",
    val status: String = "",
    val type: String = "",
    val priority: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val order: String = "",
    val parent: String = "",
    val blocking: List<String> = emptyList(),
    val blockedBy: List<String> = emptyList(),
    val body: String = "",
    val archived: Boolean = false,
    val extras: Map<String, String> = emptyMap(),
)

sealed interface ParseResult {
    data class Parsed(val bean: Bean) : ParseResult

    data class Skipped(val fileName: String, val reason: String) : ParseResult
}
