package io.github.mipmip.beansondroid.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

@Composable
fun BeanListScreen(
    viewModel: AppViewModel,
    onOpenBean: (String) -> Unit,
    onOpenRepos: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Beans")
    }
}
