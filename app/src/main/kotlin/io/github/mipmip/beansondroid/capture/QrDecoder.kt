package io.github.mipmip.beansondroid.capture

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * Pure decoding, with no Android in it, so it can be tested off a device.
 */
class QrDecoder {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
            ),
        )
    }

    fun decodeLuminance(data: ByteArray, width: Int, height: Int): String? {
        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        return decode(BinaryBitmap(HybridBinarizer(source)))
    }

    fun decodePixels(pixels: IntArray, width: Int, height: Int): String? =
        decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels))))

    private fun decode(bitmap: BinaryBitmap): String? = try {
        reader.decodeWithState(bitmap)?.text
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}
