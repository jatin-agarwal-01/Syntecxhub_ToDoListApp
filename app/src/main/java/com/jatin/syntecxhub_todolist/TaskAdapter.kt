package com.jatin.syntecxhub_todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding

/**
 * Adapter for the Task RecyclerView.
 *
 * @property tasks List of tasks to display.
 * @property onTaskChecked Callback when a task's completion status is toggled.
 * @property onEditClicked Callback when the edit button is clicked.
 * @property onDeleteClicked Callback when the delete button is clicked.
 */
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskChecked: (Task) -> Unit,
    private val onEditClicked: (Task) -> Unit,
    private val onDeleteClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            
            // Set initial checkbox state without triggering listener
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = task.isCompleted
            
            updateUI(task.isCompleted)

            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                updateUI(isChecked)
                onTaskChecked(task)
            }

            binding.btnEdit.setOnClickListener { onEditClicked(task) }
            binding.btnDelete.setOnClickListener { onDeleteClicked(task) }
            
            // Allow clicking the card to toggle completion (UX improvement)
            binding.root.setOnClickListener {
                binding.cbCompleted.isChecked = !binding.cbCompleted.isChecked
            }
        }

        private fun updateUI(isCompleted: Boolean) {
            if (isCompleted) {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.task_pending))
                binding.root.alpha = 0.6f
            } else {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.onSurface))
                binding.root.alpha = 1.0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}