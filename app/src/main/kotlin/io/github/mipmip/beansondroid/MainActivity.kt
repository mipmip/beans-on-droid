package io.github.mipmip.beansondroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mipmip.beansondroid.ui.BeansNavHost
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mipmip.beansondroid.viewmodel.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val shared = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shared.value = sharedText(intent)
        val repository = (application as BeansOnDroidApplication).beans
        setContent {
            BeansOnDroidTheme {
                val model: AppViewModel = viewModel(factory = AppViewModel.factory(repository))
                val incoming by shared.collectAsStateWithLifecycle()

                LaunchedEffect(incoming) {
                    incoming?.let {
                        model.captureUrl(it)
                        shared.value = null
                    }
                }

                BeansNavHost(viewModel = model)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText(intent)?.let { shared.value = it }
    }

    private fun sharedText(intent: Intent?): String? = when {
        intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" ->
            intent.getStringExtra(Intent.EXTRA_TEXT)

        else -> null
    }
}
