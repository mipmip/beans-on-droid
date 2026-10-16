package io.github.mipmip.beansondroid.ui

import io.github.mipmip.beansondroid.bean.Bean

object BeanMentions {

    data class Mention(val bean: Bean, val start: Int, val end: Int)

    private val RUN = Regex("[A-Za-z0-9-]+")

    fun find(text: String, lookup: (String) -> Bean?): List<Mention> =
        RUN.findAll(text).mapNotNull { match ->
            lookup(match.value)?.let { bean ->
                Mention(bean, match.range.first, match.range.last + 1)
            }
        }.toList()
}
