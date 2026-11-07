package com.joma.mediagrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.joma.mediagrab.ui.theme.LocalAppColors

const val LINK_INPUT_TAG = "link_input"
const val PARSE_BUTTON_TAG = "parse_button"

@Composable
fun LinkInput(
    link: String,
    enabled: Boolean,
    parseEnabled: Boolean,
    clearMode: Boolean,
    onTextChange: (String) -> Unit,
    onParse: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        keyboard?.hide()
        onParse()
    }
    val clear = {
        focusManager.clearFocus()
        keyboard?.hide()
        onClear()
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
        BasicTextField(
            value = link,
            onValueChange = onTextChange,
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.primary),
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
            onClick = if (clearMode) clear else submit,
            enabled = if (clearMode) enabled else enabled && parseEnabled,
            shape = RoundedCornerShape(10.dp),
            colors =
                if (clearMode) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primaryDeep,
                        contentColor = colors.onPrimaryDeep,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            modifier =
                Modifier
                    .height(36.dp)
                    .testTag(PARSE_BUTTON_TAG),
        ) {
            Text(
                text = stringResource(if (clearMode) R.string.clear else R.string.parse),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
