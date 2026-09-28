package io.github.mipmip.beansondroid.ui

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.mipmip.beansondroid.capture.FrameConverter
import io.github.mipmip.beansondroid.capture.QrDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * The camera half of scanning, as far as an emulator can prove it.
 *
 * What this asserts: CameraX binds, the camera opens, and frames reach the
 * analyzer in the format and geometry the decoder expects. That covers the
 * wiring, the permission and the frame packing.
 *
 * What it cannot assert: a decode from a live camera. The emulator's still
 * image camera renders an injected picture into a fixed sub-region and
 * distorts its aspect ratio well past what a QR code survives, so no image
 * shown to it decodes. Decoding itself is covered off-device by
 * `QrDecoderTest` and `FrameConverterTest`, the latter against frames with
 * realistic row padding. The remaining gap is a physical device pointed at a
 * real code, which is a manual check.
 */
class ScannerCameraTest {

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private data class Frame(
        val format: Int,
        val width: Int,
        val height: Int,
        val rowStride: Int,
        val pixels: Int,
        val distinctColours: Int,
    )

    @Test
    fun theCameraDeliversFramesTheDecoderCanConsume() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val seen = AtomicReference<Frame?>(null)
        val latch = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()
        val decoder = QrDecoder()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val provider = ProcessCameraProvider.getInstance(context).get()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            analysis.setAnalyzer(executor) { image ->
                try {
                    if (latch.count > 0L) {
                        val plane = image.planes.first()
                        val bytes = ByteArray(plane.buffer.remaining())
                        plane.buffer.get(bytes)
                        val pixels = FrameConverter.packPixels(
                            bytes,
                            plane.rowStride,
                            image.width,
                            image.height,
                        )
                        // Proves the packed frame is something the decoder accepts.
                        decoder.decodePixels(pixels, image.width, image.height)
                        seen.set(
                            Frame(
                                format = image.format,
                                width = image.width,
                                height = image.height,
                                rowStride = plane.rowStride,
                                pixels = pixels.size,
                                distinctColours = pixels.toHashSet().size,
                            ),
                        )
                        latch.countDown()
                    }
                } finally {
                    image.close()
                }
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                TestLifecycleOwner(),
                CameraSelector.DEFAULT_BACK_CAMERA,
                analysis,
            )
        }

        val arrived = latch.await(20, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue("no camera frame reached the analyzer within 20s", arrived)
        val frame = requireNotNull(seen.get())

        assertEquals("analysis should hand us RGBA", android.graphics.PixelFormat.RGBA_8888, frame.format)
        assertTrue("frame has no size: $frame", frame.width > 0 && frame.height > 0)
        assertEquals("packed pixel count", frame.width * frame.height, frame.pixels)
        assertTrue("row stride shorter than a row: $frame", frame.rowStride >= frame.width * 4)
        assertTrue("frame is a single flat colour, camera is probably dead: $frame", frame.distinctColours > 1)
    }
}
