package io.github.mipmip.beansondroid.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import io.github.mipmip.beansondroid.bean.BeanVocabulary

object BeanVisuals {

    fun statusColor(status: String, scheme: ColorScheme): Color = when {
        BeanVocabulary.isClosed(status) -> scheme.outline
        status == "in-progress" -> Color(0xFFB98900)
        status == "todo" -> Color(0xFF2E7D32)
        status == "draft" -> Color(0xFF1565C0)
        else -> scheme.onSurfaceVariant
    }

    fun typeColor(type: String, scheme: ColorScheme): Color = when (type) {
        "milestone" -> Color(0xFF00838F)
        "epic" -> Color(0xFF6A1B9A)
        "bug" -> Color(0xFFC62828)
        "feature" -> Color(0xFF2E7D32)
        "task" -> Color(0xFF1565C0)
        else -> scheme.onSurfaceVariant
    }
}
