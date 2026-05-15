package com.rishabh.todo.codex.feature.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Text("TaskMind", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text("Your Actionable Insights", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                        label = { Text(if (priorityFilter == "ALL") "Priority" else priorityFilter) }
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
                        label = { Text(if (statusFilter == "ALL") "Status" else statusFilter) }
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
    val gradientColors = when(task.priority) {
        TaskPriority.URGENT -> listOf(Color(0xFFFF5252), Color(0xFFFF1744))
        TaskPriority.HIGH -> listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
        TaskPriority.MEDIUM -> listOf(Color(0xFF64B5F6), Color(0xFF2196F3))
        TaskPriority.LOW -> listOf(Color(0xFF81C784), Color(0xFF4CAF50))
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(Brush.verticalGradient(gradientColors)))
            
            Column(modifier = Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(task.sourceAppDisplay, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (task.needsConfirmation) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                            Text("Confirm?", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                
                Text(task.text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                
                if (task.sender != null) {
                    Text("From: ${task.sender}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                task.dueAt?.let { dueAt ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = "Due", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(DateFormat.getDateTimeInstance().format(Date(dueAt)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task?,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onRaisePriority: (Task) -> Unit,
    onAddToCalendar: (Task) -> Unit,
    onSaveEdits: (Task, String) -> Unit, // simplified to just text
) {
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Select a task") }
        return
    }

    var text by remember(task.id) { mutableStateOf(task.text) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Task Details", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { text = it },
            label = { Text("Task Action") },
            shape = RoundedCornerShape(12.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Source App: ${task.sourceAppDisplay}", style = MaterialTheme.typography.bodyMedium)
                Text("Sender: ${task.sender ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("Due: ${task.dueAt?.let { DateFormat.getDateTimeInstance().format(Date(it)) } ?: "None"}", style = MaterialTheme.typography.bodyMedium)
                Text("Priority: ${task.priority.name}", style = MaterialTheme.typography.bodyMedium)
                Text("Keywords: ${task.triggerKeywords.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Text("Original Message:", style = MaterialTheme.typography.titleSmall)
        Text(task.rawSourceText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onComplete(task) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { 
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete") 
            }
            Button(onClick = { onDelete(task) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { 
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete") 
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onRaisePriority(task) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Raise Priority") }
            OutlinedButton(onClick = { onAddToCalendar(task) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { 
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calendar") 
            }
        }
        Button(onClick = { onSaveEdits(task, text) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { 
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Edits") 
        }
    }
}
