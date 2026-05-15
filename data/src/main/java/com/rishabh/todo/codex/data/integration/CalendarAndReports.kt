package com.rishabh.todo.codex.data.integration

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.rishabh.todo.codex.domain.model.Task
import java.util.TimeZone
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class CalendarIntegrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createEvent(task: Task): Long? {
        val start = task.dueAt ?: return null
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, start)
            put(CalendarContract.Events.DTEND, start + 30 * 60 * 1000)
            put(CalendarContract.Events.TITLE, task.text)
            put(CalendarContract.Events.DESCRIPTION, task.rawSourceText)
            put(CalendarContract.Events.CALENDAR_ID, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        return context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)?.lastPathSegment?.toLongOrNull()
    }
}

class DailyReportEmailBuilder @Inject constructor() {
    fun buildIntent(summary: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "Daily Productivity Report")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
    }
}
