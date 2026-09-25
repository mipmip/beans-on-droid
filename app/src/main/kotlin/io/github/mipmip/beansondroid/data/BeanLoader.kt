package io.github.mipmip.beansondroid.data

import io.github.mipmip.beansondroid.bean.Bean
import io.github.mipmip.beansondroid.bean.BeanParser
import io.github.mipmip.beansondroid.bean.ParseResult
import io.github.mipmip.beansondroid.index.BeanIndex
import io.github.mipmip.beansondroid.repo.BeansLayout
import java.io.File

data class LoadedBeans(
    val index: BeanIndex,
    val skipped: List<ParseResult.Skipped>,
)

class BeanLoader(private val parser: BeanParser = BeanParser()) {

    fun load(beanDir: File): LoadedBeans {
        val beans = mutableListOf<Bean>()
        val skipped = mutableListOf<ParseResult.Skipped>()

        for ((file, archived) in BeansLayout.beanFiles(beanDir)) {
            when (val result = parser.parseFile(file, archived)) {
                is ParseResult.Parsed -> beans += result.bean
                is ParseResult.Skipped -> skipped += result
            }
        }

        return LoadedBeans(BeanIndex(beans), skipped)
    }
}
