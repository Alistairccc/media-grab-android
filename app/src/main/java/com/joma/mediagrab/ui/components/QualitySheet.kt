package com.joma.mediagrab.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joma.mediagrab.R
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.util.Formatters
import com.joma.mediagrab.ui.theme.AppType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySheet(
    streams: List<Stream>,
    selected: Stream,
    onSelect: (Stream) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.quality),
                style = AppType.section,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            streams.forEach { stream ->
                val sizeBytes = stream.sizeBytes
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(stream)
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = streamLabel(stream),
                            style = AppType.body,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (sizeBytes != null) {
                            Text(
                                text =
                                    stringResource(
                                        R.string.size_mb,
                                        Formatters.sizeMb(sizeBytes),
                                    ),
                                style = AppType.monoSm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    RadioButton(
                        selected = stream == selected,
                        onClick = {
                            onSelect(stream)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}
