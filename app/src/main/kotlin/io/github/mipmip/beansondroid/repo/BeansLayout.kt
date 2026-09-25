package io.github.mipmip.beansondroid.repo

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.File

const val DEFAULT_BEAN_PATH = ".beans"
const val ARCHIVE_DIR = "archive"
private const val CONFIG_FILE = ".beans.yml"

object BeansLayout {

    fun configuredBeanPath(workingDir: File): String? {
        val config = File(workingDir, CONFIG_FILE)
        if (!config.isFile) return null
        return try {
            val options = LoaderOptions()
            val root = Yaml(SafeConstructor(options)).load<Any?>(config.readText())
            val beans = (root as? Map<*, *>)?.get("beans") as? Map<*, *>
            (beans?.get("path") as? String)?.trim()?.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    fun beanDirectory(workingDir: File): RepoResult<File> {
        val configured = configuredBeanPath(workingDir)
        if (configured != null) {
            val dir = File(workingDir, configured)
            return if (dir.isDirectory) {
                RepoResult.Success(dir)
            } else {
                RepoResult.Failure(
                    RepoError.NotABeansRepository(
                        "$CONFIG_FILE points at \"$configured\", which is not a directory in this repository",
                    ),
                )
            }
        }

        val fallback = File(workingDir, DEFAULT_BEAN_PATH)
        return if (fallback.isDirectory) {
            RepoResult.Success(fallback)
        } else {
            RepoResult.Failure(
                RepoError.NotABeansRepository("no $DEFAULT_BEAN_PATH directory in this repository"),
            )
        }
    }

    fun beanFiles(beanDir: File): List<Pair<File, Boolean>> {
        val active = beanDir.listFiles { f -> f.isFile && f.name.endsWith(".md") }
            .orEmpty()
            .map { it to false }
        val archived = File(beanDir, ARCHIVE_DIR)
            .listFiles { f -> f.isFile && f.name.endsWith(".md") }
            .orEmpty()
            .map { it to true }
        return (active + archived).sortedBy { it.first.name }
    }
}
