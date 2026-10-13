package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.ImageBlock
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary

@Composable
fun ImageBlockView(
    imageBlock: ImageBlock,
    zoomScale: Float = 1f,
    onFocus: () -> Unit = {},
    onEditProperties: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageAlignment = when (imageBlock.alignment) {
        TextAlignment.START -> Alignment.Start
        TextAlignment.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
    val scaledWidthDp = (imageBlock.widthDp * zoomScale).dp
    val scaledHeightDp = (imageBlock.heightDp * zoomScale).dp

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

        val boxModifier = when (imageBlock.wrapMode) {
            "BEHIND_TEXT" -> Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(imageCornerRadius))
                .border(imageBorderThickness, imageBorderColor.copy(alpha = 0.5f), RoundedCornerShape(imageCornerRadius))
                .graphicsLayer(alpha = 0.5f)
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
            "SQUARE" -> Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(imageCornerRadius))
                .border(imageBorderThickness + 1.dp, imageBorderColor, RoundedCornerShape(imageCornerRadius))
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
            "IN_FRONT" -> Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(imageCornerRadius))
                .border(imageBorderThickness, WordPrimary, RoundedCornerShape(imageCornerRadius))
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
            else -> Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(imageCornerRadius))
                .border(imageBorderThickness, imageBorderColor, RoundedCornerShape(imageCornerRadius))
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
        }

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
                        "IN_FRONT" -> "In Front"
                        else -> "${imageBlock.widthDp}dp"
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
