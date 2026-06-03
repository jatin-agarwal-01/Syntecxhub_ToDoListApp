package com.jatin.syntecxhub_todolist.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jatin.syntecxhub_todolist.data.model.Task

object ReminderScheduler {

    fun schedule(context: Context, task: Task) {
        val triggerAt = task.reminderTime ?: return
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(context, task)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            else
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context, taskId,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun buildPendingIntent(context: Context, task: Task) =
        PendingIntent.getBroadcast(
            context, task.id,
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
                putExtra(NotificationHelper.EXTRA_TITLE,   task.title)
                putExtra(NotificationHelper.EXTRA_DESC,    task.description)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
