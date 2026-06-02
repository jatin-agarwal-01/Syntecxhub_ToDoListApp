package com.jatin.syntecxhub_todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val onTaskChecked: (Task) -> Unit,
    private val onEditClicked: (Task) -> Unit,
    private val onDeleteClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.cbCompleted.isChecked = task.isCompleted

            // Strike-through effect if completed
            updateTitleStyle(task.isCompleted)

            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                updateTitleStyle(isChecked)
                onTaskChecked(task)
            }

            binding.btnEdit.setOnClickListener {
                onEditClicked(task)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(task)
            }
        }

        private fun updateTitleStyle(isCompleted: Boolean) {
            if (isCompleted) {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(0xFF757575.toInt())
            } else {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(0xFF000000.toInt())
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

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}