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
        Box(
            modifier = Modifier
                .width(scaledWidthDp)
                .height(scaledHeightDp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .clickable { onEditProperties() }
                .testTag("image_block_${imageBlock.id}")
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageBlock.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = imageBlock.caption.ifBlank { "Document image" },
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
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
                    Text(
                        text = "${imageBlock.widthDp}dp",
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
