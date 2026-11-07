package com.joma.mediagrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.joma.mediagrab.R
import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.util.Formatters
import com.joma.mediagrab.ui.theme.AppType
import com.joma.mediagrab.ui.theme.LocalAppColors

@Composable
fun VideoCard(
    media: MediaInfo,
    selectedStream: Stream,
    isDownloading: Boolean,
    isDone: Boolean,
    percent: Int,
    bytesPerSecond: Long,
    remainingSeconds: Long?,
    onQualityClick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(22.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CoverArt(
            media = media,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp)),
        )
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformBadge(media.platform)
                Text(
                    text = media.author,
                    style = AppType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = media.title,
                style = AppType.title,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metaLine(media, selectedStream),
                style = AppType.monoSm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QualityRow(
                media = media,
                selectedStream = selectedStream,
                enabled = !isDownloading && !isDone,
                onClick = onQualityClick,
            )
            when {
                isDone -> SavedStatus()
                isDownloading ->
                    DownloadProgress(
                        percent = percent,
                        bytesPerSecond = bytesPerSecond,
                        remainingSeconds = remainingSeconds,
                        onCancel = onCancel,
                    )

                else -> SaveButton(onSave)
            }
        }
    }
}

@Composable
private fun CoverArt(
    media: MediaInfo,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val cover =
        remember(media.coverUrl, media.platform) {
            media.coverUrl?.let { url ->
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .build()
            }
        }
    Box(
        modifier =
            modifier.background(
                Brush.linearGradient(
                    listOf(
                        colors.primaryDeep.copy(alpha = 0.55f),
                        colors.surface,
                        colors.background,
                    ),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(150.dp)
                .offset(x = 110.dp, y = (-30).dp)
                .background(colors.primary.copy(alpha = 0.28f), CircleShape),
        )
        if (cover != null) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x88000000)),
                    ),
                ),
        )
        Box(
            modifier =
                Modifier
                    .size(46.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = Formatters.duration(media.durationMs),
            style = AppType.monoSm,
            color = Color.White,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun PlatformBadge(platform: Platform) {
    Row(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(platformDot(platform), CircleShape),
        )
        Text(
            text = platformLabel(platform),
            style = AppType.meta,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun metaLine(
    media: MediaInfo,
    stream: Stream,
): String {
    val dimensions =
        if (media.width != null && media.height != null) {
            "${media.width} × ${media.height}"
        } else {
            null
        }
    val size =
        if (stream.sizeBytes != null) {
            stringResource(R.string.size_mb, Formatters.sizeMb(stream.sizeBytes))
        } else {
            null
        }
    return listOfNotNull(dimensions, size).joinToString(" · ")
}

@Composable
private fun QualityRow(
    media: MediaInfo,
    selectedStream: Stream,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = enabled && media.streams.size > 1, onClick = onClick)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.quality),
            style = AppType.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = selectedStream.qualityLabel,
            style = AppType.value,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (media.streams.size > 1) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun SaveButton(onSave: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(10.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(colors.primary, colors.secondary)))
                .clickable(onClick = onSave),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.save),
            style = AppType.button,
            color = colors.onPrimary,
        )
    }
}

@Composable
private fun SavedStatus() {
    val colors = LocalAppColors.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = colors.secondary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.saved),
            style = AppType.meta,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DownloadProgress(
    percent: Int,
    bytesPerSecond: Long,
    remainingSeconds: Long?,
    onCancel: () -> Unit,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.downloading),
                style = AppType.monoSm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$percent%",
                style = AppType.monoLg,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colors.track),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                    .background(Brush.horizontalGradient(listOf(colors.primary, colors.secondary))),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val speed = stringResource(R.string.speed_mb_s, Formatters.speedMb(bytesPerSecond))
            val eta =
                if (remainingSeconds != null) {
                    stringResource(R.string.eta_seconds, remainingSeconds)
                } else {
                    null
                }
            Text(
                text = listOfNotNull(speed, eta).joinToString(" · "),
                style = AppType.monoSm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = AppType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
