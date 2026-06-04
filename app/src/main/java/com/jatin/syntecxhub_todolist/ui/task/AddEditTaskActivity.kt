package com.jatin.syntecxhub_todolist.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jatin.syntecxhub_todolist.R
import com.jatin.syntecxhub_todolist.data.db.AppDatabase
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.data.repo.TaskRepository
import com.jatin.syntecxhub_todolist.databinding.ActivityAddEditTaskBinding
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModel
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModelFactory
import com.jatin.syntecxhub_todolist.utils.Constants
import com.jatin.syntecxhub_todolist.utils.ReminderScheduler
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.toast
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var session: SessionManager

    private var editTask: Task? = null
    private var selectedReminder: Long? = null

    companion object {
        const val EXTRA_TASK_ID        = "task_id"
        const val EXTRA_TASK_TITLE     = "task_title"
        const val EXTRA_TASK_DESC      = "task_desc"
        const val EXTRA_TASK_PRIORITY  = "task_priority"
        const val EXTRA_TASK_CATEGORY  = "task_category"
        const val EXTRA_TASK_REMINDER  = "task_reminder"
        const val EXTRA_TASK_COMPLETED = "task_completed"
        const val EXTRA_TASK_CREATED   = "task_created"

        fun startForAdd(context: Context) {
            context.startActivity(Intent(context, AddEditTaskActivity::class.java))
        }

        fun startForEdit(context: Context, task: Task) {
            context.startActivity(
                Intent(context, AddEditTaskActivity::class.java).apply {
                    putExtra(EXTRA_TASK_ID,        task.id)
                    putExtra(EXTRA_TASK_TITLE,     task.title)
                    putExtra(EXTRA_TASK_DESC,      task.description)
                    putExtra(EXTRA_TASK_PRIORITY,  task.priority)
                    putExtra(EXTRA_TASK_CATEGORY,  task.category)
                    putExtra(EXTRA_TASK_REMINDER,  task.reminderTime ?: -1L)
                    putExtra(EXTRA_TASK_COMPLETED, task.isCompleted)
                    putExtra(EXTRA_TASK_CREATED,   task.createdAt)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this, TaskViewModelFactory(applicationContext)
        )[TaskViewModel::class.java]

        session = SessionManager(this)

        // Restore edit task from intent
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId != -1) {
            editTask = Task(
                id           = taskId,
                userId       = session.getUserId(),
                title        = intent.getStringExtra(EXTRA_TASK_TITLE)    ?: "",
                description  = intent.getStringExtra(EXTRA_TASK_DESC)     ?: "",
                priority     = intent.getIntExtra(EXTRA_TASK_PRIORITY, 1),
                category     = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "General",
                reminderTime = intent.getLongExtra(EXTRA_TASK_REMINDER, -1L).takeIf { it != -1L },
                isCompleted  = intent.getBooleanExtra(EXTRA_TASK_COMPLETED, false),
                createdAt    = intent.getLongExtra(EXTRA_TASK_CREATED, System.currentTimeMillis())
            )
        }

        setupToolbar()
        setupCategorySpinner()
        setupReminderSection()
        prefillIfEditing()
        setupSaveButton()
    }

    /* ── Toolbar ──────────────────────────────────────────── */

    private fun setupToolbar() {
        binding.toolbar.title = if (editTask == null) "New Task" else "Edit Task"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    /* ── Category Spinner ─────────────────────────────────── */

    private fun setupCategorySpinner() {
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constants.DEFAULT_CATEGORIES
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerCategory.adapter = adapter
    }

    /* ── Reminder ─────────────────────────────────────────── */

    private fun setupReminderSection() {
        binding.btnSetReminder.setOnClickListener {
            if (session.isGuest()) showGuestLockedDialog()
            else showCalendarThenTimePicker(selectedReminder) { millis ->
                selectedReminder = millis
                updateReminderChip(millis)
            }
        }
        binding.btnClearReminder.setOnClickListener {
            selectedReminder = null
            binding.tvReminderChip.gone()
            binding.btnClearReminder.gone()
            toast("Reminder cleared")
        }
    }

    /* ── Pre-fill ─────────────────────────────────────────── */

    private fun prefillIfEditing() {
        val task = editTask ?: return
        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.description)
        binding.btnSave.text = "Update Task"
        when (task.priority) {
            0    -> binding.rgPriority.check(R.id.rbHigh)
            1    -> binding.rgPriority.check(R.id.rbMedium)
            else -> binding.rgPriority.check(R.id.rbLow)
        }
        val catIndex = Constants.DEFAULT_CATEGORIES.indexOf(task.category).coerceAtLeast(0)
        binding.spinnerCategory.setSelection(catIndex)
        task.reminderTime?.let { selectedReminder = it; updateReminderChip(it) }
    }

    /* ── Save ─────────────────────────────────────────────── */

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc  = binding.etDescription.text.toString().trim()

            if (title.isEmpty()) {
                binding.tilTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }
            binding.tilTitle.error = null

            val priority = when (binding.rgPriority.checkedRadioButtonId) {
                R.id.rbHigh   -> 0
                R.id.rbMedium -> 1
                else          -> 2
            }
            val category = binding.spinnerCategory.selectedItem.toString()

            if (editTask == null) {
                viewModel.addTask(title, desc, priority, category, selectedReminder)
                selectedReminder?.let { millis -> scheduleForNewTask(title, millis) }
                toast("Task added ✓")
            } else {
                val task = editTask!!
                ReminderScheduler.cancel(this, task.id)
                val updated = task.copy(
                    title        = title,
                    description  = desc,
                    priority     = priority,
                    category     = category,
                    reminderTime = selectedReminder
                )
                viewModel.updateTask(updated)
                selectedReminder?.let { ReminderScheduler.schedule(this, updated) }
                toast("Task updated ✓")
            }
            finish()
        }
    }

    /* ── Calendar + TimePicker ────────────────────────────── */

    private fun showCalendarThenTimePicker(existing: Long?, onChosen: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { existing?.let { timeInMillis = it } }

        DatePickerDialog(
            this, R.style.TimePickerTheme,
            { _, year, month, day ->
                TimePickerDialog(
                    this, R.style.TimePickerTheme,
                    { _, hour, minute ->
                        val result = Calendar.getInstance().apply {
                            set(Calendar.YEAR,         year)
                            set(Calendar.MONTH,        month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY,  hour)
                            set(Calendar.MINUTE,       minute)
                            set(Calendar.SECOND,       0)
                            set(Calendar.MILLISECOND,  0)
                        }
                        if (result.timeInMillis <= System.currentTimeMillis())
                            toast("Please pick a future date & time!")
                        else
                            onChosen(result.timeInMillis)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    session.isUse24Hr()
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    /* ── Helpers ──────────────────────────────────────────── */

    private fun updateReminderChip(millis: Long) {
        val pattern = if (session.isUse24Hr()) "EEE, MMM d • HH:mm"
        else                     "EEE, MMM d • hh:mm a"
        binding.tvReminderChip.text =
            "⏰  ${SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))}"
        binding.tvReminderChip.visible()
        binding.btnClearReminder.visible()
    }

    /**
     * After inserting a new task we wait briefly for Room to persist it,
     * then find it by title + reminderTime and schedule the alarm.
     * Uses lifecycleScope so it is automatically cancelled if the Activity is destroyed.
     */
    private fun scheduleForNewTask(title: String, millis: Long) {
        lifecycleScope.launch {
            delay(600)
            val repo = TaskRepository(AppDatabase.getInstance(this@AddEditTaskActivity).taskDao())
            repo.getTasksByUser(session.getUserId()).collect { list ->
                val t = list.firstOrNull { it.title == title && it.reminderTime == millis }
                if (t != null) {
                    ReminderScheduler.schedule(this@AddEditTaskActivity, t)
                    return@collect
                }
            }
        }
    }

    private fun showGuestLockedDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔒 Feature Locked")
            .setMessage("Reminders are only available for registered users.\n\nCreate a free account to unlock this feature!")
            .setPositiveButton("Login / Register") { _, _ ->
                startActivity(Intent(this,
                    com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java))
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }
}