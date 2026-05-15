package com.rishabh.todo.codex

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rishabh.todo.codex.core.ui.MetricCard
import com.rishabh.todo.codex.core.ui.OfflineTaskTheme
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.feature.analytics.AnalyticsScreen
import com.rishabh.todo.codex.feature.inbox.InboxScreen
import com.rishabh.todo.codex.feature.settings.SettingsScreen
import com.rishabh.todo.codex.feature.tasks.TaskDetailScreen
import com.rishabh.todo.codex.feature.tasks.TasksScreen
import com.rishabh.todo.codex.feature.transcripts.TranscriptScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfflineTaskTheme {
                TaskManagerApp(
                    viewModel = viewModel,
                    onAuthenticate = { onSuccess -> requestBiometricUnlock(onSuccess) },
                )
            }
        }
    }

    private fun requestBiometricUnlock(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            onSuccess()
            return
        }
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            },
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Offline Task Manager")
            .setSubtitle("Authenticate to view your tasks")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(promptInfo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskManagerApp(
    viewModel: MainViewModel,
    onAuthenticate: ((() -> Unit) -> Unit),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(AppTab.Dashboard) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var unlocked by remember { mutableStateOf(!state.settings.biometricLockEnabled) }
    val context = LocalContext.current

    LaunchedEffect(state.settings.biometricLockEnabled) {
        if (!state.settings.biometricLockEnabled) unlocked = true
        if (state.settings.biometricLockEnabled) unlocked = false
    }

    when {
        !state.settings.onboardingCompleted -> OnboardingScreen(
            onOpenNotificationAccess = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
            onOpenBatterySettings = { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) },
            onContinue = viewModel::completeOnboarding,
        )

        state.settings.biometricLockEnabled && !unlocked -> LockScreen(
            onUnlock = { onAuthenticate { unlocked = true } },
        )

        else -> Scaffold(
            topBar = { TopAppBar(title = { Text("Offline Task Manager") }) },
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {},
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { padding ->
            when (currentTab) {
                AppTab.Dashboard -> DashboardScreen(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onExportJson = viewModel::exportJson,
                    onExportCsv = viewModel::exportCsv,
                    onImportJson = viewModel::importJson,
                    onSendEmail = { viewModel.buildDailyEmail()?.let(context::startActivity) },
                )

                AppTab.Inbox -> InboxScreen(
                    notifications = state.inbox,
                    onApprove = viewModel::approveNotification,
                    onIgnore = viewModel::ignoreNotification,
                    onAutoApproveContact = { viewModel.trustContactFromNotification(it, ContactTrust.VIP) },
                    onIgnoreContact = { viewModel.trustContactFromNotification(it, ContactTrust.IGNORE) },
                )

                AppTab.Tasks -> {
                    if (selectedTask == null) {
                        TasksScreen(tasks = state.tasks, onTaskClick = { selectedTask = it })
                    } else {
                        TaskDetailScreen(
                            task = selectedTask,
                            onComplete = {
                                viewModel.completeTask(it)
                                selectedTask = null
                            },
                            onDelete = {
                                viewModel.deleteTask(it)
                                selectedTask = null
                            },
                            onRaisePriority = { task ->
                                viewModel.raiseTaskPriority(task)
                                // refresh selectedTask from state
                                selectedTask = state.tasks.find { it.id == task.id } ?: task
                            },
                            onAddToCalendar = viewModel::addTaskToCalendar,
                            onSaveEdits = { task, text ->
                                viewModel.saveTaskEdits(task, text)
                                selectedTask = task.copy(text = text)
                            },
                        )
                    }
                }

                AppTab.Analytics -> AnalyticsScreen(
                    snapshot = state.analytics,
                    contactCount = state.contacts.size,
                    keywordRuleCount = state.keywordRules.size,
                )

                AppTab.Settings -> SettingsScreen(
                    settings = state.settings,
                    contacts = state.contacts,
                    keywordRules = state.keywordRules,
                    onOpenNotificationAccess = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onOpenBatterySettings = {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    },
                    onReminderModeChange = viewModel::updateReminderMode,
                    onReminderIntervalChange = viewModel::updateReminderInterval,
                    onDailyReportToggle = viewModel::toggleDailyReport,
                    onBiometricToggle = viewModel::toggleBiometricLock,
                    onExportToggle = viewModel::toggleScheduledExport,
                    onExportFormatChange = viewModel::updateExportFormat,
                    onExportPathChange = viewModel::updateExportPath,
                    onImportJson = viewModel::importJson,
                    onResetOnboarding = viewModel::resetOnboarding,
                    onSetContactTrust = viewModel::setContactTrust,
                    onToggleKeywordRule = viewModel::toggleKeywordRule,
                    onDeleteKeywordRule = viewModel::deleteKeywordRule,
                    onAddKeywordRule = viewModel::createKeywordRule,
                )

                AppTab.Transcripts -> TranscriptScreen(
                    transcript = state.transcript,
                    candidates = state.transcriptCandidates,
                    onTranscriptChange = viewModel::updateTranscript,
                    onExtract = viewModel::extractTranscript,
                    onImportMine = { viewModel.importTranscriptCandidates(includeAllOwners = false) },
                    onImportAll = { viewModel.importTranscriptCandidates(includeAllOwners = true) },
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(
    onOpenNotificationAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineMedium)
        Text("Before the app can capture tasks reliably, grant notification access and relax battery restrictions.")
        Button(onClick = onOpenNotificationAccess) { Text("Open Notification Access") }
        Button(onClick = onOpenBatterySettings) { Text("Open Battery Settings") }
        Button(onClick = onContinue) { Text("Setup Complete") }
    }
}

@Composable
private fun LockScreen(
    onUnlock: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("App Locked", style = MaterialTheme.typography.headlineMedium)
        Text("Authenticate to continue.")
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier,
    state: MainUiState,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImportJson: () -> Unit,
    onSendEmail: () -> Unit,
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("TaskMind Dashboard", style = MaterialTheme.typography.headlineSmall)
                Text(state.motivationLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(title = "Pending", value = state.analytics.pendingBacklog.toString(), modifier = Modifier.weight(1f))
                MetricCard(title = "Done Today", value = state.analytics.completedToday.toString(), modifier = Modifier.weight(1f))
            }
        }
        if (state.operationMessage.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Text(
                        state.operationMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onExportJson, modifier = Modifier.weight(1f)) { Text("Export JSON") }
                Button(onClick = onExportCsv, modifier = Modifier.weight(1f)) { Text("Export CSV") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onImportJson, modifier = Modifier.weight(1f)) { Text("Import JSON") }
                OutlinedButton(onClick = onSendEmail, modifier = Modifier.weight(1f)) { Text("Daily Email") }
            }
        }
        item { Text("Recent Tasks", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        androidx.compose.foundation.lazy.items(state.tasks.take(5)) { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(task.text, style = MaterialTheme.typography.bodyMedium)
                    Text("${task.priority.name} · ${task.status.name} · ${task.sourceAppDisplay}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

private enum class AppTab(val label: String) {
    Dashboard("Dashboard"),
    Inbox("Inbox"),
    Tasks("Tasks"),
    Analytics("Analytics"),
    Settings("Settings"),
    Transcripts("Transcripts"),
}
