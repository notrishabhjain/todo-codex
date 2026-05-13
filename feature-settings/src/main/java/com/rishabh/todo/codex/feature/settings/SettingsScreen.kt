package com.rishabh.todo.codex.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onOpenNotificationAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Reminder mode: ${settings.reminderMode}", style = MaterialTheme.typography.titleMedium)
        Text("Reminder interval: ${settings.reminderIntervalMinutes} min")
        Text("Biometric lock: ${if (settings.biometricLockEnabled) "On" else "Off"}")
        Text("Scheduled export: ${settings.scheduledExportPath ?: "Not configured"}")
        Button(onClick = onOpenNotificationAccess) { Text("Grant notification access") }
        Button(onClick = onOpenBatterySettings) { Text("Disable battery optimization") }
    }
}
