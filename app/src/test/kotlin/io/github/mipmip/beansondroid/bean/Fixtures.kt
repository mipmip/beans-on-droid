package io.github.mipmip.beansondroid.bean

import java.io.File

object Fixtures {
    private fun root(): File {
        val url = requireNotNull(Fixtures::class.java.classLoader?.getResource("fixtures")) {
            "fixtures directory missing from test resources"
        }
        return File(url.toURI())
    }

    fun real(): List<File> = root().resolve("real").listFiles()!!.sortedBy { it.name }

    fun realNamed(prefix: String): File = real().first { it.name.startsWith(prefix) }

    fun synthetic(name: String): File = root().resolve("synthetic").resolve(name)
}
