package com.lawyercasediary.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.lawyercasediary.LcaApplication
import com.lawyercasediary.models.ApiResult
import java.util.concurrent.TimeUnit

class NotificationSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val app = applicationContext as LcaApplication
        val repository = app.container.notificationRepository
        
        val result = repository.getUpcomingNotifications()
        if (result is ApiResult.Success && result.data.isNotEmpty()) {
            result.data.forEach { hearing ->
                showNotification(
                    "Upcoming Hearing: ${hearing.case?.caseNumber ?: "Notice"}",
                    "Case: ${hearing.case?.title ?: "Unknown"}. Starts at ${hearing.hearingDate.take(16).replace("T", " ")}"
                )
            }
        }
        return ListenableWorker.Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hearing_alerts"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Legal Alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "hearing_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
