package com.rishabh.todo.codex.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.todo.codex.core.ui.MetricCard
import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot

@Composable
fun AnalyticsScreen(
    snapshot: AnalyticsSnapshot,
    contactCount: Int,
    keywordRuleCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricCard(title = "Completed Today", value = snapshot.completedToday.toString())
        MetricCard(title = "Completed This Week", value = snapshot.completedWeek.toString())
        MetricCard(title = "Completed This Month", value = snapshot.completedMonth.toString())
        MetricCard(title = "Pending Backlog", value = snapshot.pendingBacklog.toString())
        MetricCard(title = "Completion Rate", value = "${(snapshot.completionRate * 100).toInt()}%")
        Text("Most common source: ${snapshot.mostCommonSource}")
        Text("Ignored today: ${snapshot.ignoredToday}")
        Text("Tracked contacts: $contactCount")
        Text("Keyword rules loaded: $keywordRuleCount")
    }
}
