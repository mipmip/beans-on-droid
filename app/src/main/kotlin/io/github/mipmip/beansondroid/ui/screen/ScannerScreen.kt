package io.github.mipmip.beansondroid.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.mipmip.beansondroid.capture.CaptureResult
import io.github.mipmip.beansondroid.capture.FrameConverter
import io.github.mipmip.beansondroid.capture.QrDecoder
import io.github.mipmip.beansondroid.capture.UrlCapture
import io.github.mipmip.beansondroid.ui.Message
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var asked by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { result ->
        granted = result
        asked = true
    }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan a QR code") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                granted -> CameraPreview(
                    onDecoded = { text ->
                        when (val captured = UrlCapture.capture(text)) {
                            is CaptureResult.Found -> onResult(captured.url)
                            CaptureResult.NoUrl ->
                                notice = "That code holds no repository URL."
                        }
                    },
                )

                asked -> Message(
                    title = "Camera access is needed to scan",
                    body = "Grant camera access in system settings, or type the URL instead.",
                    actionLabel = "Back",
                    onAction = onDismiss,
                )

                else -> Message(
                    title = "Camera access is needed to scan",
                    body = "Scanning a QR code needs the camera. You can always type or " +
                        "paste the URL instead.",
                    actionLabel = "Back",
                    onAction = onDismiss,
                )
            }

            notice?.let { text ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .semantics { contentDescription = "Scan notice" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Still scanning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(onDecoded: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val decoder = remember { QrDecoder() }
    val handled = remember { AtomicBoolean(false) }
    val previewView = remember { PreviewView(context) }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null

        future.addListener(
            {
                provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer(decoder, handled, onDecoded)) }

                runCatching {
                    provider?.unbindAll()
                    provider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            runCatching { provider?.unbindAll() }
            executor.shutdown()
        }
    }
}

private fun analyzer(
    decoder: QrDecoder,
    handled: AtomicBoolean,
    onDecoded: (String) -> Unit,
) = ImageAnalysis.Analyzer { image ->
    try {
        if (!handled.get()) {
            decode(decoder, image)?.let { text ->
                if (handled.compareAndSet(false, true)) {
                    onDecoded(text)
                }
                handled.set(false)
            }
        }
    } finally {
        image.close()
    }
}

private fun decode(decoder: QrDecoder, image: ImageProxy): String? {
    val plane = image.planes.firstOrNull() ?: return null
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }

    return when (image.format) {
        PixelFormat.RGBA_8888 -> decoder.decodePixels(
            FrameConverter.packPixels(bytes, plane.rowStride, image.width, image.height),
            image.width,
            image.height,
        )

        else -> decoder.decodeLuminance(
            FrameConverter.packLuminance(bytes, plane.rowStride, image.width, image.height),
            image.width,
            image.height,
        )
    }
}

