package io.github.mipmip.beansondroid.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun ComposeContentTestRule.onAllNodesWithTextSafely(text: String): Boolean =
    onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
