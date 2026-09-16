package com.tacos78.sandglass.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacos78.sandglass.SandglassViewModel
import com.tacos78.sandglass.data.HapticPreferences
import com.tacos78.sandglass.timer.FocusTimer
import com.tacos78.sandglass.timer.TimerSnapshot
import com.tacos78.sandglass.timer.TimerStatus
import com.tacos78.sandglass.timer.formatTimer
import com.tacos78.sandglass.ui.theme.SandglassTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SandglassApp(
    viewModel: SandglassViewModel = viewModel(),
) {
    val context = LocalContext.current
    val prefs = remember(context) { HapticPreferences(context) }
    val scope = rememberCoroutineScope()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val hapticsEnabled by prefs.enabled.collectAsStateWithLifecycle(initialValue = false)
    val view = LocalView.current

    LaunchedEffect(viewModel, hapticsEnabled) {
        viewModel.secondTick.collectLatest {
            if (hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }

    SandglassScreen(
        snapshot = snapshot,
        hapticsEnabled = hapticsEnabled,
        onStartPause = viewModel::startOrPause,
        onReset = viewModel::reset,
        onDurationSelected = viewModel::setDurationMinutes,
        onHapticsChange = { enabled ->
            scope.launch { prefs.setEnabled(enabled) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandglassScreen(
    snapshot: TimerSnapshot,
    hapticsEnabled: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
) {
    val durationLocked = when (snapshot.status) {
        TimerStatus.Running, TimerStatus.Paused -> true
        TimerStatus.Idle, TimerStatus.Finished -> false
    }
    val primaryLabel = when (snapshot.status) {
        TimerStatus.Running -> "Pause"
        TimerStatus.Paused -> "Resume"
        TimerStatus.Finished -> "Start again"
        TimerStatus.Idle -> "Start"
    }
    val statusLabel = when (snapshot.status) {
        TimerStatus.Idle -> "Ready when you are"
        TimerStatus.Running -> "Focusing"
        TimerStatus.Paused -> "Paused"
        TimerStatus.Finished -> "Complete"
    }
    val primaryIcon = when (snapshot.status) {
        TimerStatus.Running -> Icons.Outlined.Pause
        TimerStatus.Idle, TimerStatus.Paused, TimerStatus.Finished -> Icons.Outlined.PlayArrow
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sandglass") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "A quiet focus timer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            HourglassVisual(
                remainingFraction = snapshot.remainingFraction,
                running = snapshot.status == TimerStatus.Running,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
            Text(
                text = formatTimer(snapshot.remainingMillis),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            DurationPresets(
                selectedMinutes = snapshot.selectedMinutes,
                enabled = !durationLocked,
                onDurationSelected = onDurationSelected,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                FilledTonalButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Outlined.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Reset")
                }
                FilledTonalButton(onClick = onStartPause) {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(primaryLabel)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                ) {
                    Text(
                        text = "Gentle ticks",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "A light haptic each second while focusing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = onHapticsChange,
                )
            }
        }
    }
}

@Composable
private fun DurationPresets(
    selectedMinutes: Int,
    enabled: Boolean,
    onDurationSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FocusTimer.PRESET_MINUTES.forEach { minutes ->
            FilterChip(
                selected = selectedMinutes == minutes,
                onClick = { onDurationSelected(minutes) },
                enabled = enabled,
                label = { Text("${minutes}m") },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SandglassScreenPreview() {
    SandglassTheme(dynamicColor = false) {
        SandglassScreen(
            snapshot = TimerSnapshot(
                totalMillis = FocusTimer.DEFAULT_DURATION_MS,
                remainingMillis = FocusTimer.DEFAULT_DURATION_MS,
                status = TimerStatus.Idle,
            ),
            hapticsEnabled = false,
            onStartPause = {},
            onReset = {},
            onDurationSelected = {},
            onHapticsChange = {},
        )
    }
}
