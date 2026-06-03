package com.jatin.syntecxhub_todolist

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onEdit:   (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

    /* ── ViewHolder ──────────────────────────────────────────── */

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {

                // ── Title + completed styling ─────────────────
                tvTitle.text = task.title
                if (task.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTitle.setTextColor(Color.parseColor("#9E9E9E"))
                    cardRoot.alpha = 0.60f
                } else {
                    tvTitle.paintFlags =
                        tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTitle.setTextColor(Color.parseColor("#1C1B1F"))
                    cardRoot.alpha = 1f
                }

                // ── Description ───────────────────────────────
                if (task.description.isNotBlank()) {
                    tvDescription.text       = task.description
                    tvDescription.visibility = View.VISIBLE
                } else {
                    tvDescription.visibility = View.GONE
                }

                // ── Created date ──────────────────────────────
                tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(task.createdAt))

                // ── Priority chip ─────────────────────────────
                val (bgHex, fgHex, label) = when (task.priority) {
                    0    -> Triple("#FFCDD2", "#B71C1C", "HIGH")
                    1    -> Triple("#FFE0B2", "#E65100", "MED")
                    else -> Triple("#C8E6C9", "#1B5E20", "LOW")
                }
                chipPriority.text = label
                chipPriority.setBackgroundColor(Color.parseColor(bgHex))
                chipPriority.setTextColor(Color.parseColor(fgHex))

                // ── Reminder chip ─────────────────────────────
                val reminderMillis = task.reminderTime
                if (reminderMillis != null && !task.isCompleted) {
                    val fmt = SimpleDateFormat("EEE hh:mm a", Locale.getDefault())
                    tvReminder.text       = "⏰ ${fmt.format(Date(reminderMillis))}"
                    tvReminder.visibility = View.VISIBLE
                } else {
                    tvReminder.visibility = View.GONE
                }

                // ── Checkbox ──────────────────────────────────
                cbDone.setOnCheckedChangeListener(null)
                cbDone.isChecked = task.isCompleted
                cbDone.setOnCheckedChangeListener { _, checked ->
                    onToggle(task.copy(isCompleted = checked))
                }

                // ── Click → edit ──────────────────────────────
                cardRoot.setOnClickListener { onEdit(task) }

                // ── Delete ────────────────────────────────────
                btnDelete.setOnClickListener { onDelete(task) }
            }
        }
    }

    /* ── Adapter overrides ───────────────────────────────────── */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(getItem(position))

    /* ── DiffUtil ────────────────────────────────────────────── */

    private companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(old: Task, new: Task) = old.id == new.id
        override fun areContentsTheSame(old: Task, new: Task) = old == new
    }
}