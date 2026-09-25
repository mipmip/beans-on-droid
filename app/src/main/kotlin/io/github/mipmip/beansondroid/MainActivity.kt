package io.github.mipmip.beansondroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mipmip.beansondroid.ui.BeansNavHost
import io.github.mipmip.beansondroid.ui.theme.BeansOnDroidTheme
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as BeansOnDroidApplication).beans
        setContent {
            BeansOnDroidTheme {
                val model: AppViewModel = viewModel(factory = AppViewModel.factory(repository))
                BeansNavHost(viewModel = model)
            }
        }
    }
}
