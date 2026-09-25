package io.github.mipmip.beansondroid.bean

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Date

private const val DELIMITER = "---"

private val TYPED_KEYS = setOf(
    "title", "status", "type", "priority", "tags",
    "created_at", "updated_at", "order", "parent", "blocking", "blocked_by",
)

class BeanParser {

    fun parseFile(file: File, archived: Boolean = false): ParseResult =
        parse(file.name, file.readText(), archived)

    fun parse(fileName: String, content: String, archived: Boolean = false): ParseResult {
        val split = splitFrontMatter(content)
            ?: return ParseResult.Skipped(fileName, "no YAML frontmatter")

        val fields = try {
            loadYaml(split.frontMatter)
        } catch (e: Exception) {
            return ParseResult.Skipped(fileName, e.message ?: "unreadable frontmatter")
        } ?: return ParseResult.Skipped(fileName, "frontmatter is not a mapping")

        val name = BeanId.parseFilename(fileName)
        val extras = LinkedHashMap<String, String>()

        val createdAt = instantOrExtra(fields["created_at"], "created_at", extras)
        val updatedAt = instantOrExtra(fields["updated_at"], "updated_at", extras)

        for ((key, value) in fields) {
            if (key !in TYPED_KEYS) {
                extras[key] = render(value)
            }
        }

        return ParseResult.Parsed(
            Bean(
                id = name.id,
                slug = name.slug,
                title = text(fields["title"]),
                status = text(fields["status"]),
                type = text(fields["type"]),
                priority = text(fields["priority"]),
                tags = list(fields["tags"]),
                createdAt = createdAt,
                updatedAt = updatedAt,
                order = text(fields["order"]),
                parent = text(fields["parent"]),
                blocking = list(fields["blocking"]),
                blockedBy = list(fields["blocked_by"]),
                body = split.body,
                archived = archived,
                extras = extras,
            ),
        )
    }

    private data class Split(val frontMatter: String, val body: String)

    private fun splitFrontMatter(content: String): Split? {
        val normalised = content.removePrefix("\uFEFF")
        val lines = normalised.lines()
        if (lines.isEmpty() || lines.first().trim() != DELIMITER) return null

        val closing = (1 until lines.size).firstOrNull { lines[it].trim() == DELIMITER }
            ?: return null

        val frontMatter = lines.subList(1, closing).joinToString("\n")
        val body = lines.subList(closing + 1, lines.size)
            .joinToString("\n")
            .removePrefix("\n")
            .trim('\n')
        return Split(frontMatter, body)
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadYaml(source: String): Map<String, Any?>? {
        if (source.isBlank()) return emptyMap()
        val options = LoaderOptions().apply {
            isAllowDuplicateKeys = false
            maxAliasesForCollections = 16
        }
        val loaded = Yaml(SafeConstructor(options)).load<Any?>(source) ?: return emptyMap()
        return loaded as? Map<String, Any?>
    }

    private fun text(value: Any?): String = when (value) {
        null -> ""
        is String -> value
        else -> render(value)
    }

    private fun list(value: Any?): List<String> = when (value) {
        null -> emptyList()
        is Iterable<*> -> value.mapNotNull { it?.let(::text) }.filter { it.isNotEmpty() }
        else -> listOf(text(value)).filter { it.isNotEmpty() }
    }

    private fun instantOrExtra(
        value: Any?,
        key: String,
        extras: MutableMap<String, String>,
    ): Instant? {
        if (value == null) return null
        toInstant(value)?.let { return it }
        extras[key] = render(value)
        return null
    }

    private fun toInstant(value: Any?): Instant? = when (value) {
        is Date -> value.toInstant()
        is String -> parseTimestamp(value)
        else -> null
    }

    private fun parseTimestamp(value: String): Instant? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return try {
            Instant.parse(trimmed)
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(trimmed).toInstant()
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    private fun render(value: Any?): String = when (value) {
        null -> ""
        is String -> value
        is Date -> value.toInstant().toString()
        is Iterable<*> -> value.joinToString(", ") { render(it) }
        is Map<*, *> -> value.entries.joinToString(", ") { "${it.key}: ${render(it.value)}" }
        else -> value.toString()
    }
}
