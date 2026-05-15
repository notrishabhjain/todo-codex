package com.rishabh.todo.codex.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TaskStatus
import java.text.DateFormat
import java.util.Date

// ─── Task List ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(tasks: List<Task>, onTaskClick: (Task) -> Unit) {
    var priorityFilter by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf("PENDING") }

    val filteredTasks = tasks.filter { task ->
        val matchesPriority = priorityFilter == "ALL" || task.priority.name == priorityFilter
        val matchesStatus = statusFilter == "ALL" || task.status.name == statusFilter
        matchesPriority && matchesStatus
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "TaskMind",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Your Actionable Insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Filter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = priorityFilter != "ALL",
                    onClick = {
                        priorityFilter = when (priorityFilter) {
                            "ALL" -> TaskPriority.URGENT.name
                            TaskPriority.URGENT.name -> TaskPriority.HIGH.name
                            TaskPriority.HIGH.name -> TaskPriority.MEDIUM.name
                            TaskPriority.MEDIUM.name -> TaskPriority.LOW.name
                            else -> "ALL"
                        }
                    },
                    label = { Text(if (priorityFilter == "ALL") "Priority" else priorityFilter) },
                )
                FilterChip(
                    selected = statusFilter != "ALL",
                    onClick = {
                        statusFilter = when (statusFilter) {
                            TaskStatus.PENDING.name -> TaskStatus.COMPLETED.name
                            TaskStatus.COMPLETED.name -> "ALL"
                            else -> TaskStatus.PENDING.name
                        }
                    },
                    label = { Text(if (statusFilter == "ALL") "Status" else statusFilter) },
                )
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No tasks here. You're all caught up!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(filteredTasks, key = { it.id.toString() }) { task ->
            TaskCard(task = task, onClick = { onTaskClick(task) })
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    val priorityColor = when (task.priority) {
        TaskPriority.URGENT -> Color(0xFFEF5350)
        TaskPriority.HIGH   -> Color(0xFFFF9800)
        TaskPriority.MEDIUM -> Color(0xFF42A5F5)
        TaskPriority.LOW    -> Color(0xFF66BB6A)
    }
    val isCompleted = task.status == TaskStatus.COMPLETED

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Priority stripe
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(if (isCompleted) MaterialTheme.colorScheme.outline else priorityColor),
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            task.sourceAppDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    // Status badge
                    when (task.status) {
                        TaskStatus.COMPLETED -> Surface(
                            color = Color(0xFF1B5E20),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                "✓ Done",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color(0xFFA5D6A7),
                            )
                        }
                        TaskStatus.ARCHIVED -> Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                "Archived",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        TaskStatus.PENDING -> if (task.needsConfirmation) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(6.dp),
                            ) {
                                Text(
                                    "Confirm?",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                        else -> {}
                    }
                }
                Text(
                    task.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                task.sender?.let {
                    Text("From: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                task.dueAt?.let { dueAt ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(dueAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

// ─── Task Detail ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task?,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onRaisePriority: (Task) -> Unit,
    onAddToCalendar: (Task) -> Unit,
    onSaveEdits: (Task, String) -> Unit,
) {
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a task from the list.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var editedText by remember(task.id) { mutableStateOf(task.text) }
    val isPending = task.status == TaskStatus.PENDING

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val statusColor = when (task.status) {
                    TaskStatus.COMPLETED -> Color(0xFF66BB6A)
                    TaskStatus.ARCHIVED  -> MaterialTheme.colorScheme.secondary
                    TaskStatus.DELETED   -> MaterialTheme.colorScheme.error
                    TaskStatus.PENDING   -> MaterialTheme.colorScheme.primary
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Task Details", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Surface(color = statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            task.status.name,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = statusColor,
                        )
                    }
                }
            }
        }

        // Editable text field
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editedText,
                onValueChange = { editedText = it },
                label = { Text("Task Action") },
                shape = RoundedCornerShape(12.dp),
                enabled = isPending,
            )
        }

        // Meta info card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetaRow("Source", task.sourceAppDisplay)
                    MetaRow("Sender", task.sender ?: "—")
                    MetaRow("Priority", task.priority.name)
                    MetaRow("Due", task.dueAt?.let { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it)) } ?: "None")
                    if (task.triggerKeywords.isNotEmpty()) {
                        MetaRow("Keywords", task.triggerKeywords.joinToString(", "))
                    }
                }
            }
        }

        // Original message
        if (task.rawSourceText.isNotBlank()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Original Message", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(task.rawSourceText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Action buttons — only show when PENDING
        if (isPending) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onComplete(task) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Complete")
                    }
                    Button(
                        onClick = { onDelete(task) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onRaisePriority(task) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Raise Priority")
                    }
                    OutlinedButton(
                        onClick = { onAddToCalendar(task) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Calendar")
                    }
                }
            }
            item {
                Button(
                    onClick = { onSaveEdits(task, editedText) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = editedText.trim() != task.text,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save Edits")
                }
            }
        } else {
            // Completed/Archived — show Delete only
            item {
                Button(
                    onClick = { onDelete(task) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete Permanently")
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
