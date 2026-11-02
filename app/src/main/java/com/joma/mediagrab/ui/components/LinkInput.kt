package com.joma.mediagrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joma.mediagrab.R
import com.joma.mediagrab.ui.theme.AppType
import com.joma.mediagrab.ui.theme.NoirAccent

@Composable
fun LinkInput(
    link: String,
    enabled: Boolean,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(start = 14.dp, end = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_link),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = link.ifEmpty { stringResource(R.string.paste_hint) },
            style = if (link.isEmpty()) AppType.body else AppType.monoMd,
            color =
                if (link.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onPaste,
            enabled = enabled,
            shape = RoundedCornerShape(10.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = NoirAccent.copy(alpha = 0.14f),
                    contentColor = Color(0xFFFF6B86),
                    disabledContainerColor = NoirAccent.copy(alpha = 0.06f),
                    disabledContentColor = NoirAccent.copy(alpha = 0.35f),
                ),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp),
        ) {
            Text(
                text = stringResource(R.string.paste),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
