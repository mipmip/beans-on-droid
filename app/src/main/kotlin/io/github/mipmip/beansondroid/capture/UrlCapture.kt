package io.github.mipmip.beansondroid.capture

sealed interface CaptureResult {
    data class Found(val url: String) : CaptureResult

    data object NoUrl : CaptureResult
}

object UrlCapture {

    private val ADDRESS = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

    private const val TRAILING = ".,;:!?)]}>\"'«»"

    private val VIEW_SEGMENTS = setOf(
        "tree", "blob", "raw", "src", "commit", "commits", "issues", "pull",
        "pulls", "merge_requests", "releases", "tags", "wiki", "actions",
        "compare", "branches", "settings",
    )

    fun capture(text: String): CaptureResult = when (val raw = extract(text)) {
        null -> CaptureResult.NoUrl
        else -> CaptureResult.Found(normalise(raw))
    }

    fun extract(text: String): String? {
        val match = ADDRESS.find(text) ?: return null
        return match.value.trimEnd { it in TRAILING }.takeIf { it.isNotEmpty() }
    }

    fun normalise(url: String): String {
        val trimmed = url.trim()
        val separator = trimmed.indexOf("://")
        if (separator < 0) return trimmed

        val scheme = trimmed.substring(0, separator + 3)
        val rest = trimmed.substring(separator + 3)
            .substringBefore('#')
            .substringBefore('?')

        val slash = rest.indexOf('/')
        if (slash < 0) return scheme + stripCredentials(rest)

        val host = stripCredentials(rest.substring(0, slash))
        val path = rest.substring(slash + 1)

        return scheme + host + "/" + truncate(path)
    }

    /**
     * Anything before an `@` in the authority is a username and possibly a
     * password. Keeping it would write a credential into the stored repository
     * list in plain text.
     */
    private fun stripCredentials(authority: String): String =
        authority.substringAfterLast('@')

    private fun truncate(path: String): String {
        val segments = path.split('/').filter { it.isNotEmpty() }
        if (segments.isEmpty()) return ""

        val marker = segments.indexOf("-")
        val cut = when {
            marker >= 0 -> marker
            else -> segments.withIndex()
                .firstOrNull { (i, s) -> i >= 2 && s in VIEW_SEGMENTS }
                ?.index
                ?: segments.size
        }
        return segments.take(cut).joinToString("/")
    }
}
