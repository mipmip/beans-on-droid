package io.github.mipmip.beansondroid.capture

/**
 * Camera frames arrive with a row stride that is often wider than the image,
 * so every row has to be copied rather than the buffer taken whole. Pure, so
 * the stride handling is testable without a camera.
 */
object FrameConverter {

    fun packLuminance(source: ByteArray, rowStride: Int, width: Int, height: Int): ByteArray {
        require(rowStride >= width) { "rowStride $rowStride is narrower than width $width" }
        if (rowStride == width) return source.copyOf(width * height)

        val out = ByteArray(width * height)
        for (y in 0 until height) {
            val from = y * rowStride
            if (from + width > source.size) break
            System.arraycopy(source, from, out, y * width, width)
        }
        return out
    }

    fun packPixels(source: ByteArray, rowStride: Int, width: Int, height: Int): IntArray {
        require(rowStride >= width * BYTES_PER_PIXEL) {
            "rowStride $rowStride is narrower than $width pixels"
        }
        val out = IntArray(width * height)
        for (y in 0 until height) {
            val rowStart = y * rowStride
            if (rowStart + width * BYTES_PER_PIXEL > source.size) break
            for (x in 0 until width) {
                val i = rowStart + x * BYTES_PER_PIXEL
                val r = source[i].toInt() and 0xFF
                val g = source[i + 1].toInt() and 0xFF
                val b = source[i + 2].toInt() and 0xFF
                out[y * width + x] = OPAQUE or (r shl 16) or (g shl 8) or b
            }
        }
        return out
    }

    private const val BYTES_PER_PIXEL = 4
    private const val OPAQUE = 0xFF shl 24
}
