package io.github.mipmip.beansondroid.repo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.net.UnknownHostException

class RepoStore(
    private val rootDir: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun workingDir(id: String): File = File(rootDir, id)

    fun isCloned(id: String): Boolean = File(workingDir(id), Constants.DOT_GIT).exists()

    suspend fun clone(id: String, url: String, token: String? = null): RepoResult<File> =
        withContext(dispatcher) {
            val target = workingDir(id)
            target.deleteRecursively()
            try {
                Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(target)
                    .setDepth(1)
                    .setCloneAllBranches(false)
                    .apply { credentials(token)?.let { setCredentialsProvider(it) } }
                    .call()
                    .use { }
                RepoResult.Success(target)
            } catch (e: Exception) {
                target.deleteRecursively()
                RepoResult.Failure(classify(e))
            }
        }

    suspend fun refresh(id: String, token: String? = null): RepoResult<File> =
        withContext(dispatcher) {
            val target = workingDir(id)
            if (!isCloned(id)) {
                return@withContext RepoResult.Failure(
                    RepoError.Unknown("repository $id has not been cloned"),
                )
            }
            try {
                Git.open(target).use { git ->
                    val branch = git.repository.branch
                    git.fetch()
                        .setRemote(Constants.DEFAULT_REMOTE_NAME)
                        .setDepth(1)
                        .setRemoveDeletedRefs(true)
                        .apply { credentials(token)?.let { setCredentialsProvider(it) } }
                        .call()

                    val remoteRef = remoteTip(git.repository, branch)
                        ?: return@withContext RepoResult.Failure(
                            RepoError.Unknown("remote branch for $branch not found after fetch"),
                        )

                    git.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef(remoteRef)
                        .call()
                }
                RepoResult.Success(target)
            } catch (e: Exception) {
                RepoResult.Failure(classify(e))
            }
        }

    fun delete(id: String): Boolean {
        val target = workingDir(id)
        if (!target.exists()) return false
        return target.deleteRecursively()
    }

    fun beanDirectory(id: String): RepoResult<File> = BeansLayout.beanDirectory(workingDir(id))

    private fun remoteTip(repository: Repository, branch: String): String? {
        val candidates = listOf(
            "${Constants.R_REMOTES}${Constants.DEFAULT_REMOTE_NAME}/$branch",
            "${Constants.R_REMOTES}${Constants.DEFAULT_REMOTE_NAME}/${Constants.HEAD}",
        )
        return candidates.firstOrNull { repository.resolve(it) != null }
    }

    private fun credentials(token: String?): UsernamePasswordCredentialsProvider? =
        token?.takeIf { it.isNotBlank() }
            ?.let { UsernamePasswordCredentialsProvider(it, "") }

    private fun classify(e: Exception): RepoError {
        val text = buildString {
            var cause: Throwable? = e
            while (cause != null) {
                append(cause.message ?: cause::class.java.simpleName)
                append(' ')
                cause = cause.cause?.takeIf { it !== cause }
            }
        }.lowercase()

        return when {
            AUTH_MARKERS.any { it in text } -> RepoError.Authentication(authMessage(text))
            hasUnknownHost(e) || NETWORK_MARKERS.any { it in text } ->
                RepoError.Network(e.message ?: "the repository could not be reached")

            e is TransportException -> RepoError.Network(
                e.message ?: "the repository could not be reached",
            )

            else -> RepoError.Unknown(e.message ?: e::class.java.simpleName)
        }
    }

    private fun authMessage(text: String): String = when {
        "not authorized" in text || "401" in text ->
            "authentication failed: the token was rejected or is missing"

        else -> "authentication failed"
    }

    private fun hasUnknownHost(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is UnknownHostException) return true
            cause = cause.cause?.takeIf { it !== cause }
        }
        return false
    }

    private companion object {
        val AUTH_MARKERS = listOf(
            "not authorized",
            "authentication is required",
            "authentication failed",
            "401",
            "403",
            "invalid credentials",
        )
        val NETWORK_MARKERS = listOf(
            "unknownhost",
            "unknown host",
            "connection refused",
            "network is unreachable",
            "timed out",
            "no route to host",
            "cannot open git-upload-pack",
        )
    }
}
