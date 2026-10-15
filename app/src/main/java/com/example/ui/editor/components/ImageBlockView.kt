package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.ImageBlock
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary
import kotlin.math.roundToInt

@Composable
fun ImageBlockView(
    imageBlock: ImageBlock,
    zoomScale: Float = 1f,
    onFocus: () -> Unit = {},
    onEditProperties: () -> Unit = {},
    onPositionAndSizeChanged: (offsetX: Float, offsetY: Float, widthDp: Int, heightDp: Int) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    var currentOffsetX by remember(imageBlock.offsetXPt) { mutableFloatStateOf(imageBlock.offsetXPt) }
    var currentOffsetY by remember(imageBlock.offsetYPt) { mutableFloatStateOf(imageBlock.offsetYPt) }
    var currentWidthDp by remember(imageBlock.widthDp) { mutableIntStateOf(imageBlock.widthDp) }
    var currentHeightDp by remember(imageBlock.heightDp) { mutableIntStateOf(imageBlock.heightDp) }

    val imageAlignment = when (imageBlock.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
    val scaledWidthDp = (currentWidthDp * zoomScale).dp
    val scaledHeightDp = (currentHeightDp * zoomScale).dp
    val isFloating = imageBlock.wrapMode == "IN_FRONT" || imageBlock.wrapMode == "IN_FRONT_OF_TEXT" || imageBlock.wrapMode == "BEHIND_TEXT" || imageBlock.wrapMode == "SQUARE"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFocus)
            .padding(vertical = 4.dp),
        horizontalAlignment = imageAlignment
    ) {
        val imageCornerRadius = imageBlock.cornerRadiusDp.coerceIn(0, 40).dp
        val imageBorderColor = try {
            Color(android.graphics.Color.parseColor(imageBlock.borderColorHex))
        } catch (_: Exception) { Color(0xFFCBD5E1) }
        val imageBorderThickness = if (imageBlock.borderThicknessDp > 0) imageBlock.borderThicknessDp.dp else 1.dp

        val colorFilter = when (imageBlock.filterMode) {
            "GRAYSCALE" -> androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
            )
            "SEPIA" -> androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            else -> null
        }

        val boxModifier = Modifier
            .offset { IntOffset((currentOffsetX * zoomScale).roundToInt(), (currentOffsetY * zoomScale).roundToInt()) }
            .width(scaledWidthDp)
            .height(scaledHeightDp)
            .then(
                if (imageBlock.wrapMode == "IN_FRONT" || imageBlock.wrapMode == "IN_FRONT_OF_TEXT") {
                    Modifier.shadow(6.dp, RoundedCornerShape(imageCornerRadius))
                } else Modifier
            )
            .clip(RoundedCornerShape(imageCornerRadius))
            .border(
                imageBorderThickness + (if (imageBlock.wrapMode == "SQUARE") 1.dp else 0.dp),
                if (imageBlock.wrapMode == "IN_FRONT" || imageBlock.wrapMode == "IN_FRONT_OF_TEXT") WordPrimary else imageBorderColor,
                RoundedCornerShape(imageCornerRadius)
            )
            .graphicsLayer(alpha = if (imageBlock.wrapMode == "BEHIND_TEXT") 0.45f else 1f)
            .clickable { onEditProperties() }
            .testTag("image_block_${imageBlock.id}")

        Box(modifier = boxModifier) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageBlock.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = imageBlock.caption.ifBlank { "Document image" },
                contentScale = ContentScale.Fit,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = imageBlock.rotationDegrees),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = WordPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                error = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = imageBlock.caption.ifBlank { "Picture Asset" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            )

            // Drag positioning handle (visible on floating images)
            if (isFloating) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .pointerInput(imageBlock.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    onPositionAndSizeChanged(currentOffsetX, currentOffsetY, currentWidthDp, currentHeightDp)
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                currentOffsetX = (currentOffsetX + dragAmount.x / zoomScale).coerceIn(-250f, 250f)
                                currentOffsetY = (currentOffsetY + dragAmount.y / zoomScale).coerceIn(-350f, 350f)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Drag to reposition image",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Interactive bottom-right corner resize handle
            Surface(
                shape = CircleShape,
                color = WordPrimary.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomStart)
                    .size(24.dp)
                    .pointerInput(imageBlock.id) {
                        detectDragGestures(
                            onDragEnd = {
                                onPositionAndSizeChanged(currentOffsetX, currentOffsetY, currentWidthDp, currentHeightDp)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val newW = (currentWidthDp + (dragAmount.x / zoomScale)).roundToInt().coerceIn(80, 500)
                            val baseRatio = if (imageBlock.aspectRatio > 0) imageBlock.aspectRatio else 1.333f
                            val newH = (newW / baseRatio).roundToInt().coerceIn(60, 450)
                            currentWidthDp = newW
                            currentHeightDp = newH
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Resize Image",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Format Overlay Chip at bottom corner
            Surface(
                shape = CircleShape,
                color = WordPrimary.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { onEditProperties() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Format Image",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    val wrapLabel = when (imageBlock.wrapMode) {
                        "SQUARE" -> "Square"
                        "BEHIND_TEXT" -> "Behind"
                        "IN_FRONT", "IN_FRONT_OF_TEXT" -> "In Front"
                        else -> "In Line"
                    }
                    Text(
                        text = wrapLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (imageBlock.caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = imageBlock.caption,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                textAlign = when (imageBlock.alignment) {
                    TextAlignment.START -> TextAlign.Start
                    TextAlignment.END -> TextAlign.End
                    else -> TextAlign.Center
                },
                modifier = Modifier.width(scaledWidthDp)
            )
        }
    }
}
