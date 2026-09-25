package io.github.mipmip.beansondroid.repo

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.SystemReader
import java.io.File
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean

private val installed = AtomicBoolean(false)

object AndroidGit {

    fun install(configDir: File) {
        if (!installed.compareAndSet(false, true)) return
        configDir.mkdirs()
        SystemReader.setInstance(AndroidSystemReader(configDir))
    }
}

internal class AndroidSystemReader(private val configDir: File) : SystemReader() {

    override fun getHostname(): String = "localhost"

    override fun getenv(variable: String?): String? = when (variable) {
        "HOME", "XDG_CONFIG_HOME" -> configDir.absolutePath
        else -> System.getenv(variable)
    }

    override fun getProperty(key: String?): String? = when (key) {
        "user.home" -> configDir.absolutePath
        else -> key?.let { System.getProperty(it) }
    }

    override fun openUserConfig(parent: Config?, fs: FS?): FileBasedConfig =
        FileBasedConfig(parent, File(configDir, "gitconfig-user"), fs)

    override fun openSystemConfig(parent: Config?, fs: FS?): FileBasedConfig =
        FileBasedConfig(parent, File(configDir, "gitconfig-system"), fs)

    override fun openJGitConfig(parent: Config?, fs: FS?): FileBasedConfig =
        FileBasedConfig(parent, File(configDir, "jgit.config"), fs)

    override fun getCurrentTime(): Long = System.currentTimeMillis()

    override fun getTimezone(whenMillis: Long): Int =
        TimeZone.getDefault().getOffset(whenMillis) / 60000

    override fun getUserConfig(): StoredConfig = openUserConfig(null, FS.DETECTED).also { it.load() }

    override fun getSystemConfig(): StoredConfig =
        openSystemConfig(null, FS.DETECTED).also { it.load() }

    override fun getJGitConfig(): StoredConfig = openJGitConfig(null, FS.DETECTED).also { it.load() }
}
