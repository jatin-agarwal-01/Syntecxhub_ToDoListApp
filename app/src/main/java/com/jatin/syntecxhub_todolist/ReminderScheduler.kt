package com.jatin.syntecxhub_todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Thin wrapper around [AlarmManager] so scheduling / cancelling logic
 * is not scattered across the codebase.
 */
object ReminderScheduler {

    /**
     * Schedule (or reschedule) an exact alarm for [task].
     * Does nothing if [Task.reminderTime] is null or already in the past.
     */
    fun schedule(context: Context, task: Task) {
        val triggerAt = task.reminderTime ?: return
        if (triggerAt <= System.currentTimeMillis()) return   // time already passed

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending       = buildPendingIntent(context, task)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires SCHEDULE_EXACT_ALARM or USE_EXACT_ALARM permission.
            // Gracefully fall back to inexact if permission not granted.
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pending
            )
        }
    }

    /** Cancel any pending alarm for the given task id. */
    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Build a matching intent (same requestCode = taskId)
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    /* ── Private helper ─────────────────────────────────────── */

    private fun buildPendingIntent(context: Context, task: Task): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
            putExtra(NotificationHelper.EXTRA_TITLE,   task.title)
            putExtra(NotificationHelper.EXTRA_DESC,    task.description)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id,              // unique request code per task
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
