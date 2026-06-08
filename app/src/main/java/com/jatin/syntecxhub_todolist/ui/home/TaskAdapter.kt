package com.jatin.syntecxhub_todolist.ui.home

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jatin.syntecxhub_todolist.R
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.databinding.ItemTaskBinding
import com.jatin.syntecxhub_todolist.utils.SessionManager
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
            val ctx     = b.root.context
            val session = SessionManager(ctx)

            b.tvTitle.text = task.title

            // Completed styling — use theme-aware colors
            val colorPrimary   = ContextCompat.getColor(ctx, R.color.text_primary)
            val colorSecondary = ContextCompat.getColor(ctx, R.color.text_secondary)

            if (task.isCompleted) {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.tvTitle.setTextColor(colorSecondary)
                b.cardRoot.alpha = 0.55f
            } else {
                b.tvTitle.paintFlags =
                    b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvTitle.setTextColor(colorPrimary)
                b.cardRoot.alpha = 1f
            }

            // Description
            if (task.description.isNotBlank()) {
                b.tvDescription.text       = task.description
                b.tvDescription.visibility = View.VISIBLE
            } else {
                b.tvDescription.visibility = View.GONE
            }

            // Created date
            b.tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(Date(task.createdAt))

            // Category chip
            b.tvCategory.text = "📁 ${task.category}"

            // Priority — chip text + stripe color
            val (bgHex, fgHex, label, stripeHex) = when (task.priority) {
                0    -> Quad("#FFCDD2", "#B71C1C", "HIGH", "#C62828")
                1    -> Quad("#FFE0B2", "#E65100", "MED",  "#E65100")
                else -> Quad("#C8E6C9", "#1B5E20", "LOW",  "#2E7D32")
            }
            b.chipPriority.text = label
            b.chipPriority.setBackgroundColor(Color.parseColor(bgHex))
            b.chipPriority.setTextColor(Color.parseColor(fgHex))

            // Left priority stripe
            b.priorityStripe.setBackgroundColor(Color.parseColor(stripeHex))
            b.priorityStripe.alpha = if (task.isCompleted) 0.3f else 1f

            // Reminder chip — respects 12/24hr preference
            val rem = task.reminderTime
            if (rem != null && !task.isCompleted) {
                val pattern = if (session.isUse24Hr()) "EEE, MMM d • HH:mm"
                              else                     "EEE, MMM d • hh:mm a"
                b.tvReminder.text       =
                    "⏰ " + SimpleDateFormat(pattern, Locale.getDefault()).format(Date(rem))
                b.tvReminder.visibility = View.VISIBLE
            } else {
                b.tvReminder.visibility = View.GONE
            }

            // Checkbox
            b.cbDone.setOnCheckedChangeListener(null)
            b.cbDone.isChecked = task.isCompleted
            b.cbDone.setOnCheckedChangeListener { _, checked ->
                onToggle(task.copy(isCompleted = checked))
            }

            b.cardRoot.setOnClickListener { onEdit(task) }
            b.btnDelete.setOnClickListener { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(
            ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(getItem(position))

    private companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(old: Task, new: Task) = old.id == new.id
        override fun areContentsTheSame(old: Task, new: Task) = old == new
    }

    /** Simple 4-element tuple to avoid Destructuring issues with Triple + extra field. */
    private data class Quad(val bg: String, val fg: String, val label: String, val stripe: String)
}
