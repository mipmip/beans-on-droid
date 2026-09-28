package io.github.mipmip.beansondroid.capture

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Test

class FrameConverterTest {

    @Test
    fun luminanceWithNoPaddingIsCopiedWhole() {
        val src = ByteArray(6) { it.toByte() }
        val out = FrameConverter.packLuminance(src, rowStride = 3, width = 3, height = 2)
        assertEquals(listOf<Byte>(0, 1, 2, 3, 4, 5), out.toList())
    }

    @Test
    fun luminancePaddingIsDropped() {
        // width 3, rowStride 5: two padding bytes per row
        val src = byteArrayOf(0, 1, 2, 9, 9, 3, 4, 5, 9, 9)
        val out = FrameConverter.packLuminance(src, rowStride = 5, width = 3, height = 2)
        assertEquals(listOf<Byte>(0, 1, 2, 3, 4, 5), out.toList())
    }

    @Test
    fun luminanceStopsAtTheEndOfAShortBuffer() {
        val src = byteArrayOf(0, 1, 2, 9, 9)
        val out = FrameConverter.packLuminance(src, rowStride = 5, width = 3, height = 4)
        assertEquals(12, out.size)
        assertEquals(listOf<Byte>(0, 1, 2), out.toList().take(3))
    }

    @Test
    fun pixelsAreReadAsRgba() {
        val src = byteArrayOf(
            0xFF.toByte(), 0, 0, 0xFF.toByte(),
            0, 0xFF.toByte(), 0, 0xFF.toByte(),
        )
        val out = FrameConverter.packPixels(src, rowStride = 8, width = 2, height = 1)
        assertEquals(0xFFFF0000.toInt(), out[0])
        assertEquals(0xFF00FF00.toInt(), out[1])
    }

    @Test
    fun pixelPaddingIsDropped() {
        val row = byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0, 0, 0, 0xFF.toByte(),
            7, 7, 7, 7,
        )
        val src = row + row
        val out = FrameConverter.packPixels(src, rowStride = 12, width = 2, height = 2)
        assertEquals(0xFFFFFFFF.toInt(), out[0])
        assertEquals(0xFF000000.toInt(), out[1])
        assertEquals(0xFFFFFFFF.toInt(), out[2])
        assertEquals(0xFF000000.toInt(), out[3])
    }

    @Test
    fun aPaddedQrFrameStillDecodes() {
        val size = 300
        val matrix = QRCodeWriter().encode(
            "https://github.com/hmans/beans",
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(EncodeHintType.MARGIN to 2),
        )
        val padding = 37
        val rowStride = size * 4 + padding
        val src = ByteArray(rowStride * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val v: Byte = if (matrix[x, y]) 0 else 0xFF.toByte()
                val i = y * rowStride + x * 4
                src[i] = v
                src[i + 1] = v
                src[i + 2] = v
                src[i + 3] = 0xFF.toByte()
            }
        }
        val pixels = FrameConverter.packPixels(src, rowStride, size, size)
        assertEquals(
            "https://github.com/hmans/beans",
            QrDecoder().decodePixels(pixels, size, size),
        )
    }

    @Test
    fun aPaddedLuminanceFrameStillDecodes() {
        val size = 300
        val matrix = QRCodeWriter().encode(
            "https://codeberg.org/owner/repo",
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(EncodeHintType.MARGIN to 2),
        )
        val padding = 21
        val rowStride = size + padding
        val src = ByteArray(rowStride * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                src[y * rowStride + x] = if (matrix[x, y]) 0 else 0xFF.toByte()
            }
        }
        val plane = FrameConverter.packLuminance(src, rowStride, size, size)
        assertEquals(
            "https://codeberg.org/owner/repo",
            QrDecoder().decodeLuminance(plane, size, size),
        )
    }
}
