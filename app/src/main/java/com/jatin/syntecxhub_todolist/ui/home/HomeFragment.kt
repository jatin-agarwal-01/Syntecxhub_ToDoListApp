package com.jatin.syntecxhub_todolist.ui.home

import android.Manifest
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
import com.google.android.material.snackbar.Snackbar
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.databinding.FragmentHomeBinding
import com.jatin.syntecxhub_todolist.ui.task.AddEditTaskActivity
import com.jatin.syntecxhub_todolist.utils.ReminderScheduler
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

        // FAB opens the new dedicated Add Task page
        binding.fabAdd.setOnClickListener {
            AddEditTaskActivity.startForAdd(requireContext())
        }
    }

    /* ── RecyclerView ──────────────────────────────────────── */

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { task ->
                viewModel.updateTask(task)
                if (task.isCompleted) ReminderScheduler.cancel(requireContext(), task.id)
            },
            onEdit   = { task ->
                // Opens the new Edit Task page
                AddEditTaskActivity.startForEdit(requireContext(), task)
            },
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
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                t: RecyclerView.ViewHolder) = false
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

                binding.progressBar.progress   = progress
                binding.tvProgressPercent.text = "$progress%"
                binding.tvStatTotal.text       = total.toString()
                binding.tvStatDone.text        = done.toString()
                binding.tvStatPending.text     = pending.toString()
                binding.tvBannerMessage.text   = when {
                    total   == 0     -> "No tasks yet. Tap + to get started! 🚀"
                    done    == total -> "All done! You crushed it today 🎉"
                    pending == 1     -> "Just 1 task left — you got this! 💪"
                    else             -> "You have $pending task${if (pending > 1) "s" else ""} pending"
                }
                binding.layoutEmpty.visibility =
                    if (tasks.isEmpty()) View.VISIBLE else View.GONE
                binding.rvTasks.visibility     =
                    if (tasks.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    /* ── Guest locked dialog ───────────────────────────────── */

    fun showGuestLockedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🔒 Feature Locked")
            .setMessage("This feature is only available for registered users.\n\nCreate a free account to unlock reminders, charts, profile & more!")
            .setPositiveButton("Login / Register") { _, _ ->
                startActivity(android.content.Intent(requireContext(),
                    com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java))
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    /* ── Helpers ───────────────────────────────────────────── */

    private fun deleteWithUndo(task: Task) {
        ReminderScheduler.cancel(requireContext(), task.id)
        viewModel.deleteTask(task)
        Snackbar.make(binding.root, "\"${task.title}\" deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.addTask(task.title, task.description, task.priority,
                    task.category, task.reminderTime)
            }.show()
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
