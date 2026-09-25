package io.github.mipmip.beansondroid.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

@Composable
fun BeanDetailScreen(
    viewModel: AppViewModel,
    beanId: String,
    onOpenBean: (String) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(beanId)
    }
}
