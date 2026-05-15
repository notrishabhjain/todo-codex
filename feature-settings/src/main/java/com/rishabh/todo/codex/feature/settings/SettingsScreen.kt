package com.rishabh.todo.codex.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Permissions ──────────────────────────────────────────────────────
        item { SectionHeader("Permissions") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenNotificationAccess, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Notification Access", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = onOpenBatterySettings, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Battery Settings", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            Button(onClick = onResetOnboarding, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
                Text("Reset Onboarding")
            }
        }

        // ── Reminders ────────────────────────────────────────────────────────
        item { SectionHeader("Reminders") }
        item {
            SettingsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(settings.reminderMode.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = {
                            onReminderModeChange(when (settings.reminderMode) {
                                ReminderMode.AGGRESSIVE -> ReminderMode.BALANCED
                                ReminderMode.BALANCED   -> ReminderMode.MINIMAL
                                ReminderMode.MINIMAL    -> ReminderMode.AGGRESSIVE
                            })
                        },
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Cycle") }
                }
            }
        }
        item {
            SettingsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Interval", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("${settings.reminderIntervalMinutes} minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = {
                            onReminderIntervalChange(when (settings.reminderIntervalMinutes) {
                                15L  -> 30L
                                30L  -> 60L
                                else -> 15L
                            })
                        },
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Cycle") }
                }
            }
        }

        // ── Security & Reports ───────────────────────────────────────────────
        item { SectionHeader("Security & Reports") }
        item {
            SettingsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Biometric Lock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(if (settings.biometricLockEnabled) "Enabled" else "Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.biometricLockEnabled, onCheckedChange = onBiometricToggle)
                }
            }
        }
        item {
            SettingsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Daily Report", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(if (settings.dailyReportEnabled) "Enabled" else "Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.dailyReportEnabled, onCheckedChange = onDailyReportToggle)
                }
            }
        }

        // ── Export / Import ──────────────────────────────────────────────────
        item { SectionHeader("Export / Import") }
        item {
            SettingsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Scheduled Export", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(if (settings.scheduledExportEnabled) settings.scheduledExportFormat.uppercase() else "Off", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.scheduledExportEnabled, onCheckedChange = onExportToggle)
                }
            }
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = exportPath,
                onValueChange = { exportPath = it },
                label = { Text("Export / Import File Path") },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onExportPathChange(exportPath) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("Save Path") }
                FilledTonalButton(onClick = { onExportFormatChange(if (settings.scheduledExportFormat == "json") "csv" else "json") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Format: ${settings.scheduledExportFormat.uppercase()}")
                }
            }
        }
        item {
            Button(onClick = onImportJson, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Text("Import JSON from Path")
            }
        }

        // ── VIP Contacts ─────────────────────────────────────────────────────
        item { SectionHeader("VIP Contacts (${contacts.size})") }
        item {
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add New Contact", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Contact Name / Sender") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = {
                                newContactTrust = when (newContactTrust) {
                                    ContactTrust.VIP           -> ContactTrust.HIGH_PRIORITY
                                    ContactTrust.HIGH_PRIORITY -> ContactTrust.NORMAL
                                    ContactTrust.NORMAL        -> ContactTrust.VIP
                                    ContactTrust.IGNORE        -> ContactTrust.VIP
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text(newContactTrust.name) }
                        Button(
                            onClick = {
                                if (newContactName.isNotBlank()) {
                                    onSetContactTrust(ContactProfile(displayName = newContactName.trim(), trust = newContactTrust), newContactTrust)
                                    newContactName = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Add Contact") }
                    }
                }
            }
        }

        items(contacts, key = { it.id }) { contact ->
            ContactRow(contact = contact, onSetTrust = { onSetContactTrust(contact, it) })
        }

        if (contacts.isEmpty()) {
            item {
                Text("No contacts tracked yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        // ── Keyword Rules ─────────────────────────────────────────────────────
        item { SectionHeader("Keyword Rules (${keywordRules.size})") }
        item {
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Keyword Rule", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newPhrase,
                        onValueChange = { newPhrase = it },
                        label = { Text("Phrase (e.g. \"send\", \"urgent\")") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = { newCategory = when (newCategory) { "action" -> "urgency"; "urgency" -> "time"; else -> "action" } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Cat: ${newCategory.replaceFirstChar { it.uppercase() }}") }
                        FilledTonalButton(
                            onClick = { newLanguage = when (newLanguage) { "en" -> "hi"; "hi" -> "hinglish"; else -> "en" } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Lang: ${newLanguage.uppercase()}") }
                    }
                    Button(
                        onClick = { if (newPhrase.isNotBlank()) { onAddKeywordRule(newPhrase, newCategory, newLanguage); newPhrase = "" } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Add Rule") }
                }
            }
        }

        items(keywordRules, key = { it.id }) { rule ->
            KeywordRuleRow(rule = rule, onToggle = { onToggleKeywordRule(rule) }, onDelete = { onDeleteKeywordRule(rule) })
        }

        if (keywordRules.isEmpty()) {
            item {
                Text("No keyword rules yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        // Bottom spacer for nav bar
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun ContactRow(contact: ContactProfile, onSetTrust: (ContactTrust) -> Unit) {
    val trustColor = when (contact.trust) {
        ContactTrust.VIP           -> MaterialTheme.colorScheme.primary
        ContactTrust.HIGH_PRIORITY -> MaterialTheme.colorScheme.tertiary
        ContactTrust.NORMAL        -> MaterialTheme.colorScheme.onSurfaceVariant
        ContactTrust.IGNORE        -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(contact.trust.name, style = MaterialTheme.typography.labelSmall, color = trustColor)
            }
            FilledTonalButton(
                onClick = {
                    onSetTrust(when (contact.trust) {
                        ContactTrust.NORMAL        -> ContactTrust.VIP
                        ContactTrust.VIP           -> ContactTrust.HIGH_PRIORITY
                        ContactTrust.HIGH_PRIORITY -> ContactTrust.IGNORE
                        ContactTrust.IGNORE        -> ContactTrust.NORMAL
                    })
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) { Text("Change") }
        }
    }
}

@Composable
private fun KeywordRuleRow(rule: KeywordRule, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("\"${rule.phrase}\"", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${rule.category.replaceFirstChar { it.uppercase() }} · ${rule.languageHint.uppercase()} · w=${rule.weight}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if (rule.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        if (rule.enabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (rule.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = onToggle, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)) {
                    Text(if (rule.enabled) "Disable" else "Enable", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
