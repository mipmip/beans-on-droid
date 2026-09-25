package io.github.mipmip.beansondroid

import android.app.Application
import io.github.mipmip.beansondroid.data.BeansRepository
import io.github.mipmip.beansondroid.repo.AndroidGit
import io.github.mipmip.beansondroid.repo.RepoStore
import io.github.mipmip.beansondroid.store.KeystoreTokenVault
import io.github.mipmip.beansondroid.store.RepoRegistry
import java.io.File

class BeansOnDroidApplication : Application() {

    lateinit var beans: BeansRepository
        private set

    override fun onCreate() {
        super.onCreate()
        AndroidGit.install(File(filesDir, "git"))
        beans = BeansRepository(
            catalog = RepoRegistry(this, KeystoreTokenVault(this)),
            store = RepoStore(File(filesDir, "repos")),
        )
    }
}
