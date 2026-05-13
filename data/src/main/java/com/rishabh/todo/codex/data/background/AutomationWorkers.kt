package com.rishabh.todo.codex.data.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rishabh.todo.codex.data.di.BackgroundDependenciesEntryPoint
import com.rishabh.todo.codex.domain.model.AppSettings
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val AUTOMATION_CHANNEL_ID = "automation_channel"

class AutomationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(settings: AppSettings) {
        ensureChannel()
        if (settings.scheduledExportEnabled && !settings.scheduledExportPath.isNullOrBlank()) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "scheduled-export",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<ExportWorker>(12, TimeUnit.HOURS).build(),
            )
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("scheduled-export")
        }

        if (settings.dailyReportEnabled) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily-report",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DailyReportWorker>(24, TimeUnit.HOURS).build(),
            )
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("daily-report")
        }
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                AUTOMATION_CHANNEL_ID,
                "Automation",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }
}

class ExportWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ensureChannel()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            BackgroundDependenciesEntryPoint::class.java,
        )
        val settings = entryPoint.settingsRepository().observeSettings().first()
        val path = settings.scheduledExportPath ?: return Result.success()
        val result = if (settings.scheduledExportFormat.equals("csv", ignoreCase = true)) {
            entryPoint.exportRepository().exportCsv(path)
        } else {
            entryPoint.exportRepository().exportJson(path)
        }
        val success = result.isSuccess
        notify(
            title = if (success) "Scheduled export completed" else "Scheduled export failed",
            body = result.getOrNull() ?: result.exceptionOrNull()?.message.orEmpty(),
        )
        return if (success) Result.success() else Result.retry()
    }

    private fun notify(title: String, body: String) {
        NotificationManagerCompat.from(applicationContext).notify(
            301,
            NotificationCompat.Builder(applicationContext, AUTOMATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentTitle(title)
                .setContentText(body.ifBlank { "Automation finished." })
                .build(),
        )
    }

    private fun ensureChannel() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                AUTOMATION_CHANNEL_ID,
                "Automation",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }
}

class DailyReportWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ensureChannel()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            BackgroundDependenciesEntryPoint::class.java,
        )
        val settings = entryPoint.settingsRepository().observeSettings().first()
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.now(), zone)
        if (now.hour != settings.emailReportHour) return Result.success()
        val snapshot = entryPoint.analyticsRepository().observeSnapshot().first()
        NotificationManagerCompat.from(applicationContext).notify(
            302,
            NotificationCompat.Builder(applicationContext, AUTOMATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Daily report ready")
                .setContentText("Done ${snapshot.completedToday}, pending ${snapshot.pendingBacklog}. Open app to draft the email.")
                .build(),
        )
        return Result.success()
    }

    private fun ensureChannel() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                AUTOMATION_CHANNEL_ID,
                "Automation",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }
}
