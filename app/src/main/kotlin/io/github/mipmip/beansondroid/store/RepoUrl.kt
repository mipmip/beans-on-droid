package io.github.mipmip.beansondroid.store

object RepoUrl {
    fun validate(url: String): String? {
        val trimmed = url.trim()
        return when {
            trimmed.isEmpty() -> "Enter the repository's clone URL."
            trimmed.startsWith("git@") || trimmed.startsWith("ssh://") ->
                "SSH URLs are not supported. Use the https:// clone URL."

            !trimmed.startsWith("https://") && !trimmed.startsWith("http://") ->
                "The URL must start with https://."

            trimmed.removePrefix("https://").removePrefix("http://").isEmpty() ->
                "That URL has no host."

            else -> null
        }
    }
}
