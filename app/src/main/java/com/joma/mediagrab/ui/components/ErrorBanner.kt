package com.joma.mediagrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joma.mediagrab.R
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.ui.theme.AppType
import com.joma.mediagrab.ui.theme.LocalAppColors

@Composable
fun ErrorBanner(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(start = 12.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = errorMessage(error),
            style = AppType.meta,
            color = Color(0xFFFF8FA3),
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Text(
                text = stringResource(R.string.retry),
                style = AppType.label,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun errorMessage(error: AppError): String =
    when (error) {
        AppError.InvalidLink -> stringResource(R.string.invalid_link)
        AppError.UnsupportedPlatform -> stringResource(R.string.unsupported_platform)
        AppError.UnsupportedContent -> stringResource(R.string.unsupported_content)
        AppError.NoStream -> stringResource(R.string.no_stream)
        is AppError.Network -> stringResource(R.string.parse_failed)
        is AppError.Parse -> stringResource(R.string.parse_failed)
        is AppError.Download -> stringResource(R.string.download_failed)
        AppError.StorageFull -> stringResource(R.string.storage_full)
    }
