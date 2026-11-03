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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joma.mediagrab.R
import com.joma.mediagrab.ui.theme.AppType
import com.joma.mediagrab.ui.theme.NoirAccent

const val LINK_INPUT_TAG = "link_input"
const val PARSE_BUTTON_TAG = "parse_button"

@Composable
fun LinkInput(
    link: String,
    enabled: Boolean,
    showParse: Boolean,
    onTextChange: (String) -> Unit,
    onPaste: () -> Unit,
    onParse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        keyboard?.hide()
        onParse()
    }
    val textStyle =
        (if (link.isEmpty()) AppType.body else AppType.monoMd).copy(
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
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
            BasicTextField(
                value = link,
                onValueChange = onTextChange,
                enabled = enabled,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(NoirAccent),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go,
                    ),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(LINK_INPUT_TAG),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (link.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_hint),
                                style = AppType.body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        inner()
                    }
                },
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    onPaste()
                },
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
        if (showParse) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(R.string.parse),
                    style = AppType.meta,
                    color =
                        if (enabled) {
                            NoirAccent
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier =
                        Modifier
                            .testTag(PARSE_BUTTON_TAG)
                            .minimumInteractiveComponentSize()
                            .clickable(enabled = enabled) { submit() }
                            .padding(horizontal = 8.dp),
                )
            }
        }
    }
}
