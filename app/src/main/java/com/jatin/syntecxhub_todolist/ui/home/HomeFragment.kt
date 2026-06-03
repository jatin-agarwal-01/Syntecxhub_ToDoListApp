package com.jatin.syntecxhub_todolist.ui.home

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jatin.syntecxhub_todolist.R
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.databinding.BottomSheetTaskBinding
import com.jatin.syntecxhub_todolist.databinding.FragmentHomeBinding
import com.jatin.syntecxhub_todolist.utils.Constants
import com.jatin.syntecxhub_todolist.utils.ReminderScheduler
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.toast
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter
    private lateinit var session: SessionManager

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        binding.tvDate.text =
            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        binding.tvWelcome.text = "Hi, ${session.getUserName()} 👋"

        requestNotifPermission()
        setupRecyclerView()
        setupSwipeToDelete()
        observeTasks()

        binding.fabAdd.setOnClickListener { openBottomSheet(null) }
    }

    /* ── RecyclerView ──────────────────────────────────────── */

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { task ->
                viewModel.updateTask(task)
                if (task.isCompleted) ReminderScheduler.cancel(requireContext(), task.id)
            },
            onEdit   = { task -> openBottomSheet(task) },
            onDelete = { task -> deleteWithUndo(task) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter       = adapter
        binding.rvTasks.setHasFixedSize(false)
    }

    private fun setupSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos = vh.adapterPosition
                if (pos != RecyclerView.NO_POSITION) deleteWithUndo(adapter.currentList[pos])
            }
        }).attachToRecyclerView(binding.rvTasks)
    }

    /* ── Observe ───────────────────────────────────────────── */

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { tasks ->
                adapter.submitList(tasks)

                val total    = tasks.size
                val done     = tasks.count { it.isCompleted }
                val pending  = total - done
                val progress = if (total > 0) (done * 100) / total else 0

                binding.progressBar.progress      = progress
                binding.tvProgressPercent.text    = "$progress%"
                binding.tvStatTotal.text          = total.toString()
                binding.tvStatDone.text           = done.toString()
                binding.tvStatPending.text        = pending.toString()
                binding.tvBannerMessage.text = when {
                    total   == 0     -> "No tasks yet. Tap + to get started! 🚀"
                    done    == total -> "All done! You crushed it today 🎉"
                    pending == 1     -> "Just 1 task left — you got this! 💪"
                    else             -> "You have $pending task${if (pending > 1) "s" else ""} pending"
                }

                if (tasks.isEmpty()) {
                    binding.layoutEmpty.visible()
                    binding.rvTasks.gone()
                } else {
                    binding.layoutEmpty.gone()
                    binding.rvTasks.visible()
                }
            }
        }
    }

    /* ── Bottom Sheet ──────────────────────────────────────── */

    private fun openBottomSheet(editTask: Task?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheet  = BottomSheetTaskBinding.inflate(layoutInflater)
        dialog.setContentView(sheet.root)
        dialog.behavior.isDraggable = true
        dialog.behavior.peekHeight  = 600

        var selectedReminder: Long? = editTask?.reminderTime

        // Category spinner
        val categories = Constants.DEFAULT_CATEGORIES
        val catAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        sheet.spinnerCategory.adapter = catAdapter

        if (editTask != null) {
            sheet.tvSheetTitle.text = "Edit Task"
            sheet.etTitle.setText(editTask.title)
            sheet.etDescription.setText(editTask.description)
            sheet.btnSave.text = "Update Task"
            val catIndex = categories.indexOf(editTask.category).coerceAtLeast(0)
            sheet.spinnerCategory.setSelection(catIndex)
            when (editTask.priority) {
                0    -> sheet.rgPriority.check(R.id.rbHigh)
                1    -> sheet.rgPriority.check(R.id.rbMedium)
                else -> sheet.rgPriority.check(R.id.rbLow)
            }
            editTask.reminderTime?.let { updateReminderChip(sheet, it) }
        }

        // Reminder button — locked for guests
        sheet.btnSetReminder.setOnClickListener {
            if (session.isGuest()) {
                showGuestLockedDialog()
            } else {
                showTimePicker(selectedReminder) { millis ->
                    selectedReminder = millis
                    updateReminderChip(sheet, millis)
                }
            }
        }

        sheet.btnClearReminder.setOnClickListener {
            selectedReminder = null
            sheet.tvReminderChip.gone()
            sheet.btnClearReminder.gone()
            toast("Reminder cleared")
        }

        sheet.btnSave.setOnClickListener {
            val title = sheet.etTitle.text.toString().trim()
            val desc  = sheet.etDescription.text.toString().trim()
            if (title.isEmpty()) { sheet.tilTitle.error = "Title cannot be empty"; return@setOnClickListener }
            sheet.tilTitle.error = null

            val priority = when (sheet.rgPriority.checkedRadioButtonId) {
                R.id.rbHigh   -> 0
                R.id.rbMedium -> 1
                else          -> 2
            }
            val category = sheet.spinnerCategory.selectedItem.toString()

            if (editTask == null) {
                viewModel.addTask(title, desc, priority, category, selectedReminder)
                selectedReminder?.let { scheduleForNewTask(title, desc, it) }
                toast("Task added ✓")
            } else {
                ReminderScheduler.cancel(requireContext(), editTask.id)
                val updated = editTask.copy(
                    title        = title,
                    description  = desc,
                    priority     = priority,
                    category     = category,
                    reminderTime = selectedReminder
                )
                viewModel.updateTask(updated)
                selectedReminder?.let { ReminderScheduler.schedule(requireContext(), updated) }
                toast("Task updated ✓")
            }
            dialog.dismiss()
        }

        sheet.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /* ── TimePicker ────────────────────────────────────────── */

    private fun showTimePicker(existing: Long?, onChosen: (Long) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Which day?")
            .setItems(arrayOf("📅  Today", "📅  Tomorrow")) { _, which ->
                val cal = Calendar.getInstance().apply {
                    existing?.let { timeInMillis = it }
                }
                TimePickerDialog(
                    requireContext(), R.style.TimePickerTheme,
                    { _, h, m ->
                        val result = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, which)
                            set(Calendar.HOUR_OF_DAY, h)
                            set(Calendar.MINUTE,      m)
                            set(Calendar.SECOND,      0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (result.timeInMillis <= System.currentTimeMillis()) {
                            toast("Please pick a future time!")
                        } else {
                            onChosen(result.timeInMillis)
                        }
                    },
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
                ).show()
            }.show()
    }

    /* ── Guest locked dialog ───────────────────────────────── */

    fun showGuestLockedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🔒 Feature Locked")
            .setMessage("This feature is available for registered users only.\n\nCreate a free account to unlock reminders, charts, profile & more!")
            .setPositiveButton("Login / Register") { _, _ ->
                startActivity(
                    android.content.Intent(
                        requireContext(),
                        com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java
                    )
                )
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    /* ── Helpers ───────────────────────────────────────────── */

    private fun updateReminderChip(sheet: BottomSheetTaskBinding, millis: Long) {
        val fmt = SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault())
        sheet.tvReminderChip.text = "⏰  ${fmt.format(Date(millis))}"
        sheet.tvReminderChip.visible()
        sheet.btnClearReminder.visible()
    }

    private fun scheduleForNewTask(title: String, desc: String, millis: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { list ->
                val t = list.firstOrNull { it.title == title && it.reminderTime == millis }
                if (t != null) {
                    ReminderScheduler.schedule(requireContext(), t)
                    return@collect
                }
            }
        }
    }

    private fun deleteWithUndo(task: Task) {
        ReminderScheduler.cancel(requireContext(), task.id)
        viewModel.deleteTask(task)
        Snackbar.make(binding.root, "\"${task.title}\" deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.addTask(task.title, task.description, task.priority, task.category, task.reminderTime)
                task.reminderTime?.let { scheduleForNewTask(task.title, task.description, it) }
            }.show()
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
