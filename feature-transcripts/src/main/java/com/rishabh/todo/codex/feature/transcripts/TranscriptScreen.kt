package com.rishabh.todo.codex.feature.transcripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.domain.model.TranscriptCandidateTask

@Composable
fun TranscriptScreen(
    transcript: String,
    candidates: List<TranscriptCandidateTask>,
    onTranscriptChange: (String) -> Unit,
    onExtract: () -> Unit,
    onImportMine: () -> Unit,
    onImportAll: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = transcript,
            onValueChange = onTranscriptChange,
            minLines = 6,
            label = { Text("Paste meeting transcript") },
        )
        Button(onClick = onExtract) { Text("Extract actions") }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onImportMine) { Text("Import My Tasks") }
            Button(onClick = onImportAll) { Text("Import All Tasks") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(candidates) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.title)
                        Text(item.description)
                        Text("Owner: ${item.owner ?: "Unclear"}")
                    }
                }
            }
        }
    }
}
