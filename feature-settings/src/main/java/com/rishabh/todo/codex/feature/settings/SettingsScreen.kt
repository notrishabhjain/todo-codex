package com.rishabh.todo.codex.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.ReminderMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    contacts: List<ContactProfile>,
    onOpenNotificationAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onReminderModeChange: (ReminderMode) -> Unit,
    onReminderIntervalChange: (Long) -> Unit,
    onDailyReportToggle: (Boolean) -> Unit,
    onExportToggle: (Boolean) -> Unit,
    onExportFormatChange: (String) -> Unit,
    onSetContactTrust: (ContactProfile, ContactTrust) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Reminder mode: ${settings.reminderMode}", style = MaterialTheme.typography.titleMedium)
        Text("Reminder interval: ${settings.reminderIntervalMinutes} min")
        Button(onClick = {
            onReminderModeChange(
                when (settings.reminderMode) {
                    ReminderMode.AGGRESSIVE -> ReminderMode.BALANCED
                    ReminderMode.BALANCED -> ReminderMode.MINIMAL
                    ReminderMode.MINIMAL -> ReminderMode.AGGRESSIVE
                },
            )
        }) { Text("Cycle Reminder Mode") }
        Button(onClick = {
            onReminderIntervalChange(
                when (settings.reminderIntervalMinutes) {
                    15L -> 30L
                    30L -> 60L
                    else -> 15L
                },
            )
        }) { Text("Cycle Reminder Interval") }
        Text("Daily report: ${if (settings.dailyReportEnabled) "Enabled" else "Disabled"}")
        Button(onClick = { onDailyReportToggle(!settings.dailyReportEnabled) }) { Text("Toggle Daily Report") }
        Text("Biometric lock: ${if (settings.biometricLockEnabled) "On" else "Off"}")
        Text("Scheduled export: ${if (settings.scheduledExportEnabled) settings.scheduledExportFormat.uppercase() else "Disabled"}")
        Text("Export path: ${settings.scheduledExportPath ?: "Not configured"}")
        Button(onClick = { onExportToggle(!settings.scheduledExportEnabled) }) { Text("Toggle Scheduled Export") }
        Button(onClick = {
            onExportFormatChange(if (settings.scheduledExportFormat == "json") "csv" else "json")
        }) { Text("Switch Export Format") }
        Button(onClick = onOpenNotificationAccess) { Text("Grant notification access") }
        Button(onClick = onOpenBatterySettings) { Text("Disable battery optimization") }
        Text("Contacts", style = MaterialTheme.typography.titleMedium)
        contacts.forEach { contact ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(contact.displayName)
                    Text("Trust: ${contact.trust} | Weight: ${contact.learnedWeight}")
                    Button(onClick = {
                        onSetContactTrust(
                            contact,
                            when (contact.trust) {
                                ContactTrust.NORMAL -> ContactTrust.VIP
                                ContactTrust.VIP -> ContactTrust.HIGH_PRIORITY
                                ContactTrust.HIGH_PRIORITY -> ContactTrust.IGNORE
                                ContactTrust.IGNORE -> ContactTrust.NORMAL
                            },
                        )
                    }) { Text("Cycle Trust") }
                }
            }
        }
    }
}
