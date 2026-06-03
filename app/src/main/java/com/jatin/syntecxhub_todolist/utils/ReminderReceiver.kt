package com.jatin.syntecxhub_todolist.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) return
        NotificationHelper.showReminderNotification(
            context,
            intent.getIntExtra(NotificationHelper.EXTRA_TASK_ID, 0),
            intent.getStringExtra(NotificationHelper.EXTRA_TITLE)  ?: "Task Reminder",
            intent.getStringExtra(NotificationHelper.EXTRA_DESC)   ?: ""
        )
    }
}
