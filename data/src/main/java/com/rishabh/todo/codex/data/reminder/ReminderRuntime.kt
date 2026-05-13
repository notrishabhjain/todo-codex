package com.rishabh.todo.codex.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rishabh.todo.codex.data.di.TaskRepositoryEntryPoint
import com.rishabh.todo.codex.domain.engine.ReminderScheduler
import com.rishabh.todo.codex.domain.model.ReminderPolicy
import com.rishabh.todo.codex.domain.model.Task
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val CHANNEL_ID = "reminder_channel"
private const val FOREGROUND_ID = 42

class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {
    override suspend fun schedule(policy: ReminderPolicy, tasks: List<Task>) {
        ReminderForegroundService.ensureChannel(context)
        val work = PeriodicWorkRequestBuilder<ReminderWorker>(
            policy.intervalMinutes.coerceAtLeast(15L),
            TimeUnit.MINUTES,
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "task-reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            work,
        )
        context.startForegroundService(Intent(context, ReminderForegroundService::class.java))
    }
}

class ReminderForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel(this)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Anti-procrastination shield active")
            .setContentText("Tracking pending tasks and keeping them visible.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(FOREGROUND_ID, notification)
        return START_STICKY
    }

    companion object {
        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Task reminders",
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )
        }
    }
}

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            TaskRepositoryEntryPoint::class.java,
        )
        val taskRepository = entryPoint.taskRepository()
        ReminderForegroundService.ensureChannel(applicationContext)
        val tasks = taskRepository.observePendingTasks().first()
        val top = tasks.firstOrNull()
        val content = when {
            top == null -> "No pending tasks right now."
            tasks.size > 1 -> "${tasks.size} tasks pending. Top focus: ${top.title}"
            else -> "${top.title} is still pending."
        }
        NotificationManagerCompat.from(applicationContext).notify(
            99,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("Stay on it")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build(),
        )
        return Result.success()
    }
}
