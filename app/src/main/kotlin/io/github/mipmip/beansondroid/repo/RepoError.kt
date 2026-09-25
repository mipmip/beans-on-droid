package io.github.mipmip.beansondroid.repo

sealed interface RepoError {
    val message: String

    data class Authentication(override val message: String) : RepoError

    data class Network(override val message: String) : RepoError

    data class NotABeansRepository(override val message: String) : RepoError

    data class Unknown(override val message: String) : RepoError
}

sealed interface RepoResult<out T> {
    data class Success<T>(val value: T) : RepoResult<T>

    data class Failure(val error: RepoError) : RepoResult<Nothing>
}

fun <T> RepoResult<T>.valueOrNull(): T? = (this as? RepoResult.Success<T>)?.value

fun <T> RepoResult<T>.errorOrNull(): RepoError? = (this as? RepoResult.Failure)?.error
