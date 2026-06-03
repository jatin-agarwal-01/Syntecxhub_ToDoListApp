package com.jatin.syntecxhub_todolist.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jatin.syntecxhub_todolist.ui.home.MainActivity

object NotificationHelper {

    const val CHANNEL_ID    = "task_reminder_channel"
    const val CHANNEL_NAME  = "Task Reminders"
    const val EXTRA_TITLE   = "extra_task_title"
    const val EXTRA_DESC    = "extra_task_desc"
    const val EXTRA_TASK_ID = "extra_task_id"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your ToDo tasks"
                enableLights(true)
                enableVibration(true)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        taskId: Int,
        title: String,
        description: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰  $title")
            .setContentText(description.ifBlank { "Time to work on this task!" })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(description.ifBlank { "Time to work on this task!" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId, notif)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
