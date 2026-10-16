package io.github.mipmip.beansondroid.ui

import io.noties.markwon.ext.tables.TableRowSpan
import java.lang.reflect.Field

internal object MarkwonTables {

    private val CELLS: Field = TableRowSpan::class.java
        .getDeclaredField("cells")
        .apply { isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    fun cells(row: TableRowSpan): List<TableRowSpan.Cell> = CELLS.get(row) as List<TableRowSpan.Cell>
}
