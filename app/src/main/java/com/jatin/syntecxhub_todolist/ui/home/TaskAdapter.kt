package com.jatin.syntecxhub_todolist.ui.home

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onEdit:   (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

    inner class TaskViewHolder(private val b: ItemTaskBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(task: Task) {
            b.tvTitle.text = task.title

            // Completed styling
            if (task.isCompleted) {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.tvTitle.setTextColor(Color.parseColor("#9E9E9E"))
                b.cardRoot.alpha = 0.60f
            } else {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvTitle.setTextColor(Color.parseColor("#1C1B1F"))
                b.cardRoot.alpha = 1f
            }

            // Description
            if (task.description.isNotBlank()) {
                b.tvDescription.text       = task.description
                b.tvDescription.visibility = View.VISIBLE
            } else {
                b.tvDescription.visibility = View.GONE
            }

            // Date
            b.tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(Date(task.createdAt))

            // Category chip
            b.tvCategory.text = "📁 ${task.category}"

            // Priority chip
            val (bg, fg, label) = when (task.priority) {
                0    -> Triple("#FFCDD2", "#B71C1C", "HIGH")
                1    -> Triple("#FFE0B2", "#E65100", "MED")
                else -> Triple("#C8E6C9", "#1B5E20", "LOW")
            }
            b.chipPriority.text = label
            b.chipPriority.setBackgroundColor(Color.parseColor(bg))
            b.chipPriority.setTextColor(Color.parseColor(fg))

            // Reminder chip
            val rem = task.reminderTime
            if (rem != null && !task.isCompleted) {
                b.tvReminder.text       = "⏰ " + SimpleDateFormat("EEE hh:mm a", Locale.getDefault()).format(Date(rem))
                b.tvReminder.visibility = View.VISIBLE
            } else {
                b.tvReminder.visibility = View.GONE
            }

            // Checkbox
            b.cbDone.setOnCheckedChangeListener(null)
            b.cbDone.isChecked = task.isCompleted
            b.cbDone.setOnCheckedChangeListener { _, checked -> onToggle(task.copy(isCompleted = checked)) }

            b.cardRoot.setOnClickListener { onEdit(task) }
            b.btnDelete.setOnClickListener { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(getItem(position))

    private companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(old: Task, new: Task) = old.id == new.id
        override fun areContentsTheSame(old: Task, new: Task) = old == new
    }
}
