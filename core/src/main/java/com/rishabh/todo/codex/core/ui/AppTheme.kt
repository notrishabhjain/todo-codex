package com.rishabh.todo.codex.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val CodexColors = darkColorScheme(
    primary             = Color(0xFF82B1FF),
    onPrimary           = Color(0xFF00174E),
    primaryContainer    = Color(0xFF0A2E7A),
    onPrimaryContainer  = Color(0xFFD4E3FF),
    secondary           = Color(0xFF80CBC4),
    onSecondary         = Color(0xFF003733),
    secondaryContainer  = Color(0xFF004F4A),
    onSecondaryContainer= Color(0xFF9EF2EB),
    tertiary            = Color(0xFFCF94DA),
    onTertiary          = Color(0xFF4A0058),
    tertiaryContainer   = Color(0xFF630072),
    onTertiaryContainer = Color(0xFFEFB0F9),
    error               = Color(0xFFEF9A9A),
    onError             = Color(0xFF690005),
    errorContainer      = Color(0xFF7D1E1E),
    onErrorContainer    = Color(0xFFFFDAD6),
    background          = Color(0xFF0F1117),
    onBackground        = Color(0xFFE2E2E6),
    surface             = Color(0xFF1A1C22),
    onSurface           = Color(0xFFE2E2E6),
    surfaceVariant      = Color(0xFF1E2130),
    onSurfaceVariant    = Color(0xFFA8ABBA),
    outline             = Color(0xFF525770),
    outlineVariant      = Color(0xFF2C2F42),
)

@Composable
fun OfflineTaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CodexColors,
        content = { Surface(content = content) },
    )
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
