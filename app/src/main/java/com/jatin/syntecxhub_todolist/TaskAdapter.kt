package com.jatin.syntecxhub_todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding

class TaskAdapter(
    private val onComplete: (Task) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                
                // Strikethrough for completed tasks
                if (task.isCompleted) {
                    taskTitle.paintFlags = taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    root.alpha = 0.6f
                } else {
                    taskTitle.paintFlags = taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    root.alpha = 1.0f
                }

                // Priority badge colors
                val (color, label) = when (task.priority) {
                    0 -> Pair("#FF0000", "HIGH")
                    1 -> Pair("#FFA500", "MED")
                    else -> Pair("#00AA00", "LOW")
                }
                priorityBadge.text = label
                priorityBadge.setBackgroundColor(android.graphics.Color.parseColor(color))

                // Due date
                if (task.dueDate != null) {
                    val dueText = java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)
                        .format(task.dueDate)
                    dueDateText.text = dueText
                    dueDateText.visibility = android.view.View.VISIBLE
                } else {
                    dueDateText.visibility = android.view.View.GONE
                }

                // Checkbox listener
                completeCheckbox.setOnCheckedChangeListener(null)
                completeCheckbox.isChecked = task.isCompleted
                completeCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    onComplete(task.copy(isCompleted = isChecked))
                }

                // Edit click
                root.setOnClickListener {
                    onEdit(task)
                }

                // Delete button
                deleteBtn.setOnClickListener {
                    onDelete(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        class DiffCallback : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
        }
    }
}
