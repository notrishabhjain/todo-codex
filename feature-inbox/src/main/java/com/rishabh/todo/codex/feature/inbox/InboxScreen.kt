package com.rishabh.todo.codex.feature.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.NotificationRecord

@Composable
fun InboxScreen(
    notifications: List<NotificationRecord>,
    onApprove: (NotificationRecord) -> Unit,
    onIgnore: (NotificationRecord) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notifications) { record ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(record.sender ?: record.sourceType.name, style = MaterialTheme.typography.titleMedium)
                    Text(record.rawText, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onApprove(record) }) { Text("Create Task") }
                        Button(onClick = { onIgnore(record) }) { Text("Ignore") }
                    }
                }
            }
        }
    }
}
