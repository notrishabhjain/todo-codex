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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskPriority
import java.text.DateFormat
import java.util.Date

@Composable
fun TasksScreen(tasks: List<Task>, onTaskClick: (Task) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tasks) { task ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onTaskClick(task) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Priority: ${task.priority}")
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
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(task?.title ?: "Select a task", style = MaterialTheme.typography.headlineSmall)
        Text(task?.description.orEmpty())
        Text("Source: ${task?.sourceType ?: "-"}")
        Text("Sender: ${task?.sender ?: "-"}")
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
