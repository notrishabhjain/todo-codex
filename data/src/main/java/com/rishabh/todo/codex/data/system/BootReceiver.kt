package com.rishabh.todo.codex.data.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rishabh.todo.codex.data.reminder.ReminderWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val work = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "task-reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            work,
        )
    }
}
