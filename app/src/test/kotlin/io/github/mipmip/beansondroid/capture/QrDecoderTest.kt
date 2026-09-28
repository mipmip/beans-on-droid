package io.github.mipmip.beansondroid.capture

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrDecoderTest {

    private val decoder = QrDecoder()

    private fun qrPixels(text: String, size: Int = 400): Pair<IntArray, Int> {
        val matrix = QRCodeWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(EncodeHintType.MARGIN to 2),
        )
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        return pixels to size
    }

    private fun qrLuminance(text: String, size: Int = 400): Triple<ByteArray, Int, Int> {
        val (pixels, side) = qrPixels(text, size)
        val luminance = ByteArray(side * side) { i ->
            if (pixels[i] == 0xFF000000.toInt()) 0 else 255.toByte()
        }
        return Triple(luminance, side, side)
    }

    @Test
    fun decodesAUrlFromPixels() {
        val (pixels, size) = qrPixels("https://github.com/hmans/beans.git")
        assertEquals("https://github.com/hmans/beans.git", decoder.decodePixels(pixels, size, size))
    }

    @Test
    fun decodesAUrlFromALuminancePlane() {
        val (data, w, h) = qrLuminance("https://github.com/hmans/beans")
        assertEquals("https://github.com/hmans/beans", decoder.decodeLuminance(data, w, h))
    }

    @Test
    fun decodesAPageUrlThatNeedsNormalising() {
        val url = "https://github.com/hmans/beans/tree/main/pkg?x=1"
        val (pixels, size) = qrPixels(url)
        val decoded = decoder.decodePixels(pixels, size, size)
        assertEquals(url, decoded)
        assertEquals(
            CaptureResult.Found("https://github.com/hmans/beans"),
            UrlCapture.capture(decoded!!),
        )
    }

    @Test
    fun decodesTextThatIsNotAUrl() {
        val (pixels, size) = qrPixels("just some text")
        assertEquals("just some text", decoder.decodePixels(pixels, size, size))
        assertEquals(CaptureResult.NoUrl, UrlCapture.capture("just some text"))
    }

    @Test
    fun aBlankImageDecodesToNothing() {
        val size = 200
        val white = IntArray(size * size) { 0xFFFFFFFF.toInt() }
        assertNull(decoder.decodePixels(white, size, size))
    }

    @Test
    fun theDecoderIsReusableAcrossCalls() {
        repeat(3) { i ->
            val url = "https://forge.test/owner/repo$i"
            val (pixels, size) = qrPixels(url)
            assertEquals(url, decoder.decodePixels(pixels, size, size))
        }
        val blank = IntArray(100 * 100) { 0xFFFFFFFF.toInt() }
        assertNull(decoder.decodePixels(blank, 100, 100))
        val (pixels, size) = qrPixels("https://after.test/a/b")
        assertEquals("https://after.test/a/b", decoder.decodePixels(pixels, size, size))
    }
}
