package com.joma.mediagrab.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joma.mediagrab.BuildConfig
import com.joma.mediagrab.R
import com.joma.mediagrab.ui.components.ErrorBanner
import com.joma.mediagrab.ui.components.LinkInput
import com.joma.mediagrab.ui.components.QualitySheet
import com.joma.mediagrab.ui.components.VideoCard
import com.joma.mediagrab.ui.theme.AppType
import com.joma.mediagrab.ui.theme.NoirAccent
import java.util.Locale

private const val GITHUB_URL = "https://github.com/Alistairccc/media-grab-android"

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val link by viewModel.link.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val notificationPermissionGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.saved)

    var showQuality by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is HomeUiState.Done) {
            snackbarHostState.showSnackbar(savedMessage)
        }
    }

    LaunchedEffect(state) {
        if (state !is HomeUiState.Ready) {
            showQuality = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp),
        ) {
            AppHeader(onAbout = { showAbout = true })
            Spacer(Modifier.height(13.dp))
            LinkInput(
                link = link,
                enabled = state !is HomeUiState.Parsing && state !is HomeUiState.Downloading,
                parseEnabled =
                    link.isNotBlank() &&
                        state !is HomeUiState.Parsing &&
                        state !is HomeUiState.Downloading &&
                        state !is HomeUiState.Ready &&
                        state !is HomeUiState.Done,
                onTextChange = viewModel::onLinkChange,
                onParse = viewModel::onParse,
            )
            Spacer(Modifier.height(13.dp))
            Box(Modifier.weight(1f)) {
                when (val current = state) {
                    HomeUiState.Idle -> Unit
                    HomeUiState.Parsing -> ParsingState()
                    is HomeUiState.Ready ->
                        VideoCard(
                            media = current.media,
                            selectedStream = current.selectedStream,
                            isDownloading = false,
                            isDone = false,
                            percent = 0,
                            bytesPerSecond = 0,
                            remainingSeconds = null,
                            onQualityClick = { showQuality = true },
                            onSave = {
                                if (!notificationPermissionGranted) {
                                    notificationPermission.launch(
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    )
                                }
                                viewModel.onSave()
                            },
                            onCancel = {},
                        )

                    is HomeUiState.Downloading ->
                        VideoCard(
                            media = current.media,
                            selectedStream = current.selectedStream,
                            isDownloading = true,
                            isDone = false,
                            percent = current.percent,
                            bytesPerSecond = current.bytesPerSecond,
                            remainingSeconds = current.remainingSeconds,
                            onQualityClick = {},
                            onSave = {},
                            onCancel = viewModel::onCancel,
                        )

                    is HomeUiState.Done ->
                        VideoCard(
                            media = current.media,
                            selectedStream = current.selectedStream,
                            isDownloading = false,
                            isDone = true,
                            percent = 100,
                            bytesPerSecond = 0,
                            remainingSeconds = null,
                            onQualityClick = {},
                            onSave = {},
                            onCancel = {},
                        )

                    is HomeUiState.Failed ->
                        ErrorBanner(
                            error = current.error,
                            onRetry = viewModel::onRetry,
                        )
                }
            }
        }
    }

    val ready = state as? HomeUiState.Ready
    if (showQuality && ready != null) {
        QualitySheet(
            streams = ready.media.streams,
            selected = ready.selectedStream,
            onSelect = viewModel::onSelectStream,
            onDismiss = { showQuality = false },
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun AppHeader(onAbout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name).uppercase(Locale.ROOT),
            style = AppType.brand,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .size(5.dp)
                .background(NoirAccent, CircleShape),
        )
        Spacer(Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.about), style = AppType.meta) },
                    onClick = {
                        expanded = false
                        onAbout()
                    },
                )
            }
        }
    }
}

@Composable
private fun ParsingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = NoirAccent,
            strokeWidth = 2.dp,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.parsing),
            style = AppType.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                    style = AppType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.compliance_note),
                    style = AppType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.license_note),
                    style = AppType.monoSm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable { uriHandler.openUri(GITHUB_URL) }
                            .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.github_repo),
                            style = AppType.meta,
                            color = NoirAccent,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "↗",
                            style = AppType.meta,
                            color = NoirAccent,
                        )
                    }
                    Text(
                        text = GITHUB_URL.removePrefix("https://"),
                        style = AppType.monoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
