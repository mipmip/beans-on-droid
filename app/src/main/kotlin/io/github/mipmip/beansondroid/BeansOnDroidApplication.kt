package io.github.mipmip.beansondroid

import android.app.Application
import io.github.mipmip.beansondroid.repo.AndroidGit
import java.io.File

class BeansOnDroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidGit.install(File(filesDir, "git"))
    }
}
