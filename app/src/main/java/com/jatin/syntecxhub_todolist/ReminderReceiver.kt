package com.jatin.syntecxhub_todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the AlarmManager broadcast and delegates to [NotificationHelper]
 * to post the heads-up notification.
 *
 * Also registered for BOOT_COMPLETED so alarms survive device restarts.
 * (Re-scheduling on boot requires reading tasks from Room — keep it simple here:
 *  on boot we just let the user re-set reminders; the receiver is registered so
 *  the manifest entry is valid even if we expand later.)
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted — alarms are wiped by the OS.
                // For now we silently accept; a full implementation would
                // re-read Room and re-schedule all pending alarms here.
            }
            else -> {
                val taskId = intent.getIntExtra(
                    NotificationHelper.EXTRA_TASK_ID, 0
                )
                val title = intent.getStringExtra(
                    NotificationHelper.EXTRA_TITLE
                ) ?: "Task Reminder"
                val desc = intent.getStringExtra(
                    NotificationHelper.EXTRA_DESC
                ) ?: ""

                NotificationHelper.showReminderNotification(context, taskId, title, desc)
            }
        }
    }
}
