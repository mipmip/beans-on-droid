package io.github.mipmip.beansondroid.repo

import org.eclipse.jgit.api.Git
import java.io.File

class LocalRemote(private val dir: File) {

    val url: String get() = dir.toURI().toString()

    fun init(): LocalRemote {
        dir.mkdirs()
        Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
            write(".beans.yml", "beans:\n    path: .beans\n")
            File(dir, ".beans").mkdirs()
            write(".beans/x-aaaa--first.md", bean("First bean"))
            git.add().addFilepattern(".").call()
            git.commit().setMessage("first").setSign(false).call()
        }
        return this
    }

    fun commit(path: String, content: String, message: String) {
        Git.open(dir).use { git ->
            write(path, content)
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).setSign(false).call()
        }
    }

    fun resetHardToNewRoot(message: String) {
        Git.open(dir).use { git ->
            write("unrelated.txt", "start over")
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).setAmend(true).setSign(false).call()
        }
    }

    fun commitCount(): Int = Git.open(dir).use { it.log().call().count() }

    private fun write(path: String, content: String) {
        val file = File(dir, path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    companion object {
        fun bean(title: String, status: String = "todo") = """
            ---
            title: $title
            status: $status
            type: task
            ---

            Body of $title.
        """.trimIndent() + "\n"
    }
}
