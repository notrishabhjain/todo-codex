package com.rishabh.todo.codex.feature.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TaskStatus
import java.text.DateFormat
import java.util.Date

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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        priorityFilter = when (priorityFilter) {
                            "ALL" -> TaskPriority.CRITICAL.name
                            TaskPriority.CRITICAL.name -> TaskPriority.HIGH.name
                            TaskPriority.HIGH.name -> TaskPriority.MEDIUM.name
                            TaskPriority.MEDIUM.name -> TaskPriority.LOW.name
                            else -> "ALL"
                        }
                    }) { Text("Priority: $priorityFilter") }
                    Button(onClick = {
                        statusFilter = when (statusFilter) {
                            TaskStatus.PENDING.name -> TaskStatus.COMPLETED.name
                            TaskStatus.COMPLETED.name -> TaskStatus.ARCHIVED.name
                            TaskStatus.ARCHIVED.name -> "ALL"
                            else -> TaskStatus.PENDING.name
                        }
                    }) { Text("Status: $statusFilter") }
                }
                Text("Showing ${filteredTasks.size} of ${tasks.size} tasks")
            }
        }
        items(filteredTasks) { task ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onTaskClick(task) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Priority: ${task.priority} | Status: ${task.status}")
                    Text("Due: ${task.dueAtEpochMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) } ?: "None"}")
                }
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    task: Task?,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onArchive: (Task) -> Unit,
    onRaisePriority: (Task) -> Unit,
    onAddToCalendar: (Task) -> Unit,
    onSaveEdits: (Task, String, String, String, String) -> Unit,
    onClearDueDate: (Task) -> Unit,
    onPostponeOneDay: (Task) -> Unit,
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var notes by remember(task?.id) { mutableStateOf(task?.notes.orEmpty()) }
    var tagsCsv by remember(task?.id) { mutableStateOf(task?.tags?.joinToString(", ").orEmpty()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(task?.title ?: "Select a task", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = tagsCsv,
            onValueChange = { tagsCsv = it },
            label = { Text("Tags (comma separated)") },
        )
        Text("Source: ${task?.sourceType ?: "-"}")
        Text("Sender: ${task?.sender ?: "-"}")
        Text("Due: ${task?.dueAtEpochMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) } ?: "None"}")
        Text("Original: ${task?.originalNotificationText ?: "-"}")
        Text("Confidence: ${task?.confidence ?: 0f}")
        if (task != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onComplete(task) }) { Text("Complete") }
                Button(onClick = { onDelete(task) }) { Text("Delete") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onArchive(task) }) { Text("Archive") }
                Button(onClick = { onRaisePriority(task) }) { Text(nextPriorityLabel(task.priority)) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onClearDueDate(task) }) { Text("Clear Due") }
                Button(onClick = { onPostponeOneDay(task) }) { Text("Push 1 Day") }
            }
            Button(onClick = { onSaveEdits(task, title, description, notes, tagsCsv) }) { Text("Save Edits") }
            Button(onClick = { onAddToCalendar(task) }) { Text("Add To Calendar") }
        }
    }
}

private fun nextPriorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.LOW -> "Set Medium"
    TaskPriority.MEDIUM -> "Set High"
    TaskPriority.HIGH -> "Set Critical"
    TaskPriority.CRITICAL -> "Keep Critical"
}
