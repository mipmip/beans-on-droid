package io.github.mipmip.beansondroid.bean

data class BeanName(val id: String, val slug: String)

object BeanId {
    fun parseFilename(name: String): BeanName {
        val stem = name.removeSuffix(".md")

        val doubleDash = stem.indexOf("--")
        if (doubleDash > 0) {
            return BeanName(stem.substring(0, doubleDash), stem.substring(doubleDash + 2))
        }

        val dot = stem.indexOf('.')
        if (dot > 0) {
            return BeanName(stem.substring(0, dot), stem.substring(dot + 1))
        }

        val dash = stem.indexOf('-')
        if (dash > 0) {
            return BeanName(stem.substring(0, dash), stem.substring(dash + 1))
        }

        return BeanName(stem, "")
    }
}
