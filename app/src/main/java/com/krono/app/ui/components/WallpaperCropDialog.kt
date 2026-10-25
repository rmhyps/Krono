package com.krono.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun WallpaperCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropConfirmed: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Screen aspect ratio (height / width)
    val screenAspectRatio = remember(configuration) {
        val w = configuration.screenWidthDp.toFloat().coerceAtLeast(1f)
        val h = configuration.screenHeightDp.toFloat().coerceAtLeast(1f)
        h / w
    }

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessingCrop by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        isLoading = true
        loadedBitmap = withContext(Dispatchers.IO) {
            loadAndOrientBitmap(context, imageUri)
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Atur Posisi Wallpaper",
                    color = KronoOnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Geser dan cubit/zoom gambar agar pas dengan layar HP",
                    color = KronoOnSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading || loadedBitmap == null) {
                        CircularProgressIndicator(color = KronoPrimary)
                    } else {
                        val bitmap = loadedBitmap!!
                        InteractiveCropViewport(
                            bitmap = bitmap,
                            targetAspectRatio = screenAspectRatio,
                            isProcessing = isProcessingCrop,
                            onPerformCrop = { zoomFactor, panOffset, viewWidthPx, viewHeightPx ->
                                isProcessingCrop = true
                                val cropped = cropBitmapToViewport(
                                    source = bitmap,
                                    zoomFactor = zoomFactor,
                                    panOffset = panOffset,
                                    viewWidthPx = viewWidthPx,
                                    viewHeightPx = viewHeightPx
                                )
                                onCropConfirmed(cropped)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessingCrop
                    ) {
                        Text("Batal", color = KronoOnSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveCropViewport(
    bitmap: Bitmap,
    targetAspectRatio: Float,
    isProcessing: Boolean,
    onPerformCrop: (zoom: Float, pan: Offset, viewW: Float, viewH: Float) -> Unit
) {
    val density = LocalDensity.current

    // State for interactive user gestures
    var zoomFactor by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val maxBoxW = constraints.maxWidth.toFloat()
        val maxBoxH = constraints.maxHeight.toFloat()

        // Calculate viewport frame matching screen aspect ratio
        var frameW = maxBoxW * 0.9f
        var frameH = frameW * targetAspectRatio
        if (frameH > maxBoxH * 0.85f) {
            frameH = maxBoxH * 0.85f
            frameW = frameH / targetAspectRatio
        }

        val frameWidthDp = with(density) { frameW.toDp() }
        val frameHeightDp = with(density) { frameH.toDp() }

        // Base scale to fill the crop frame without black margins
        val baseScale = max(frameW / bitmap.width.toFloat(), frameH / bitmap.height.toFloat())
        val effectiveScale = baseScale * zoomFactor

        val renderW = bitmap.width * effectiveScale
        val renderH = bitmap.height * effectiveScale

        val maxPanX = max(0f, (renderW - frameW) / 2f)
        val maxPanY = max(0f, (renderH - frameH) / 2f)

        // Ensure panOffset is always clamped within valid boundaries
        val clampedPanX = panOffset.x.coerceIn(-maxPanX, maxPanX)
        val clampedPanY = panOffset.y.coerceIn(-maxPanY, maxPanY)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Viewport Box
            Box(
                modifier = Modifier
                    .size(frameWidthDp, frameHeightDp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, KronoPrimary, RoundedCornerShape(8.dp))
                    .pointerInput(bitmap, frameW, frameH) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val newZoom = (zoomFactor * gestureZoom).coerceIn(1f, 5f)
                            zoomFactor = newZoom

                            val currentEffectiveScale = baseScale * newZoom
                            val currentRenderW = bitmap.width * currentEffectiveScale
                            val currentRenderH = bitmap.height * currentEffectiveScale

                            val currentMaxPanX = max(0f, (currentRenderW - frameW) / 2f)
                            val currentMaxPanY = max(0f, (currentRenderH - frameH) / 2f)

                            panOffset = Offset(
                                x = (panOffset.x + pan.x).coerceIn(-currentMaxPanX, currentMaxPanX),
                                y = (panOffset.y + pan.y).coerceIn(-currentMaxPanY, currentMaxPanY)
                            )
                        }
                    }
            ) {
                val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    clipRect {
                        val renderLeft = (frameW - renderW) / 2f + clampedPanX
                        val renderTop = (frameH - renderH) / 2f + clampedPanY

                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(renderLeft.roundToInt(), renderTop.roundToInt()),
                            dstSize = IntSize(renderW.roundToInt(), renderH.roundToInt())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons for this frame
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        zoomFactor = 1f
                        panOffset = Offset.Zero
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tengah", color = KronoTertiary, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onPerformCrop(zoomFactor, Offset(clampedPanX, clampedPanY), frameW, frameH)
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = KronoOnPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Gunakan Gambar", color = KronoOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun cropBitmapToViewport(
    source: Bitmap,
    zoomFactor: Float,
    panOffset: Offset,
    viewWidthPx: Float,
    viewHeightPx: Float
): Bitmap {
    val baseScale = max(viewWidthPx / source.width.toFloat(), viewHeightPx / source.height.toFloat())
    val effectiveScale = baseScale * zoomFactor

    val renderW = source.width * effectiveScale
    val renderH = source.height * effectiveScale

    val renderLeft = (viewWidthPx - renderW) / 2f + panOffset.x
    val renderTop = (viewHeightPx - renderH) / 2f + panOffset.y

    val cropRenderX = -renderLeft
    val cropRenderY = -renderTop

    val origCropX = (cropRenderX / effectiveScale).toInt().coerceIn(0, source.width - 1)
    val origCropY = (cropRenderY / effectiveScale).toInt().coerceIn(0, source.height - 1)

    val origCropW = (viewWidthPx / effectiveScale).toInt().coerceIn(1, source.width - origCropX)
    val origCropH = (viewHeightPx / effectiveScale).toInt().coerceIn(1, source.height - origCropY)

    return Bitmap.createBitmap(source, origCropX, origCropY, origCropW, origCropH)
}

private fun loadAndOrientBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val resolver = context.contentResolver

        // Read orientation
        val orientation = resolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        // Decode bounds first to check size
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        // Subsample large bitmaps (max 2048px to prevent OOM)
        val maxDimension = 2048
        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        // Apply rotation if needed
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        } else {
            decoded
        }
    } catch (_: Exception) {
        null
    }
}
