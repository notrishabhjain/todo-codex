package com.rishabh.todo.codex.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.model.ReminderMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    contacts: List<ContactProfile>,
    keywordRules: List<KeywordRule>,
    onOpenNotificationAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onReminderModeChange: (ReminderMode) -> Unit,
    onReminderIntervalChange: (Long) -> Unit,
    onDailyReportToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onExportToggle: (Boolean) -> Unit,
    onExportFormatChange: (String) -> Unit,
    onExportPathChange: (String) -> Unit,
    onImportJson: () -> Unit,
    onResetOnboarding: () -> Unit,
    onSetContactTrust: (ContactProfile, ContactTrust) -> Unit,
    onToggleKeywordRule: (KeywordRule) -> Unit,
    onDeleteKeywordRule: (KeywordRule) -> Unit,
    onAddKeywordRule: (String, String, String) -> Unit,
) {
    var newPhrase by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("action") }
    var newLanguage by remember { mutableStateOf("en") }
    var exportPath by remember(settings.scheduledExportPath) { mutableStateOf(settings.scheduledExportPath ?: "") }
    var newContactName by remember { mutableStateOf("") }
    var newContactTrust by remember { mutableStateOf(ContactTrust.VIP) }

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
        Button(onClick = { onBiometricToggle(!settings.biometricLockEnabled) }) { Text("Toggle Biometric Lock") }
        Text("Scheduled export: ${if (settings.scheduledExportEnabled) settings.scheduledExportFormat.uppercase() else "Disabled"}")
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = exportPath,
            onValueChange = { exportPath = it },
            label = { Text("Export / Import Path") },
        )
        Button(onClick = { onExportPathChange(exportPath) }) { Text("Save Export Path") }
        Button(onClick = { onExportToggle(!settings.scheduledExportEnabled) }) { Text("Toggle Scheduled Export") }
        Button(onClick = {
            onExportFormatChange(if (settings.scheduledExportFormat == "json") "csv" else "json")
        }) { Text("Switch Export Format") }
        Button(onClick = onImportJson) { Text("Import JSON From Path") }
        Button(onClick = onOpenNotificationAccess) { Text("Grant notification access") }
        Button(onClick = onOpenBatterySettings) { Text("Disable battery optimization") }
        Button(onClick = onResetOnboarding) { Text("Reset Onboarding") }
        Text("Add Keyword Rule", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newPhrase,
            onValueChange = { newPhrase = it },
            label = { Text("Phrase") },
        )
        Button(onClick = {
            newCategory = when (newCategory) {
                "action" -> "urgency"
                "urgency" -> "time"
                else -> "action"
            }
        }) { Text("Category: ${newCategory.replaceFirstChar { it.uppercase() }}") }
        Button(onClick = {
            newLanguage = when (newLanguage) {
                "en" -> "hi"
                "hi" -> "hinglish"
                else -> "en"
            }
        }) { Text("Language: ${newLanguage.uppercase()}") }
        Button(onClick = {
            onAddKeywordRule(newPhrase, newCategory, newLanguage)
            newPhrase = ""
        }) { Text("Add Rule") }
        Text("Contacts", style = MaterialTheme.typography.titleMedium)
        // --- Add new contact ---
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newContactName,
            onValueChange = { newContactName = it },
            label = { Text("Contact Name") },
        )
        Button(onClick = {
            newContactTrust = when (newContactTrust) {
                ContactTrust.VIP -> ContactTrust.HIGH_PRIORITY
                ContactTrust.HIGH_PRIORITY -> ContactTrust.NORMAL
                ContactTrust.NORMAL -> ContactTrust.VIP
                ContactTrust.IGNORE -> ContactTrust.VIP
            }
        }) { Text("Trust Level: ${newContactTrust.name}") }
        Button(
            onClick = {
                if (newContactName.isNotBlank()) {
                    onSetContactTrust(
                        ContactProfile(displayName = newContactName.trim(), trust = newContactTrust),
                        newContactTrust,
                    )
                    newContactName = ""
                }
            }
        ) { Text("Add Contact") }
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
        Text("Keyword Rules", style = MaterialTheme.typography.titleMedium)
        keywordRules.forEach { rule ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${rule.phrase} (${rule.category})")
                    Text("Language: ${rule.languageHint} | Weight: ${rule.weight}")
                    Text("State: ${if (rule.enabled) "Enabled" else "Disabled"}")
                    Button(onClick = { onToggleKeywordRule(rule) }) { Text(if (rule.enabled) "Disable Rule" else "Enable Rule") }
                    Button(onClick = { onDeleteKeywordRule(rule) }) { Text("Delete Rule") }
                }
            }
        }
    }
}
