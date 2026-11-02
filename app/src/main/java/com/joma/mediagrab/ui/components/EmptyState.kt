package com.joma.mediagrab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joma.mediagrab.R
import com.joma.mediagrab.ui.theme.AppType

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0x2EFFFFFF),
                            style =
                                Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                                ),
                        )
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "↓",
                style = AppType.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.empty_hint),
            style = AppType.body,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.empty_support),
            style = AppType.meta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
