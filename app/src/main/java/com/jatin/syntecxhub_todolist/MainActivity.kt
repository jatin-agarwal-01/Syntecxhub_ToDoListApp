package com.jatin.syntecxhub_todolist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jatin.syntecxhub_todolist.databinding.ActivityMainBinding

/**
 * Main activity for the To-Do List application.
 * Manages the task list, persistence, and primary UI interactions.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private var taskList = mutableListOf<Task>()
    
    private val gson = Gson()
    private val sharedPrefs by lazy { getSharedPreferences("todo_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadTasks()
        setupRecyclerView()
        setupFab()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = taskList,
            onTaskChecked = { saveTasks() },
            onEditClicked = { task -> showTaskDialog(task) },
            onDeleteClicked = { task -> showDeleteConfirmation(task) }
        )

        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        setupSwipeToDelete()
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }
        
        // Shrink/Extend FAB on scroll for better UX
        binding.rvTasks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.fabAddTask.shrink()
                else if (dy < 0) binding.fabAddTask.extend()
            }
        })
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Change bindingAdapterPosition to adapterPosition
                val position = viewHolder.adapterPosition

                // Add a safety check to ensure the position is still valid
                if (position != RecyclerView.NO_POSITION) {
                    val deletedTask = taskList[position]
                    performDeletion(position, deletedTask)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvTasks)
    }

    private fun showTaskDialog(taskToEdit: Task? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)
        val tilTitle = dialogView.findViewById<TextInputLayout>(R.id.tilTaskTitle)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        if (taskToEdit != null) {
            tvDialogTitle.text = "Edit Task"
            etTitle.setText(taskToEdit.title)
        }

        val dialog = AlertDialog.Builder(this, R.style.Theme_ToDoListApp)
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null to override closing behavior for validation
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    tilTitle.error = "Task title cannot be empty"
                } else {
                    if (taskToEdit == null) {
                        addTask(title)
                    } else {
                        updateTask(taskToEdit, title)
                    }
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun addTask(title: String) {
        val newTask = Task(id = System.currentTimeMillis(), title = title)
        taskList.add(0, newTask)
        taskAdapter.notifyItemInserted(0)
        binding.rvTasks.scrollToPosition(0)
        saveTasks()
        updateEmptyState()
    }

    private fun updateTask(task: Task, newTitle: String) {
        val index = taskList.indexOf(task)
        if (index != -1) {
            task.title = newTitle
            taskAdapter.notifyItemChanged(index)
            saveTasks()
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                val position = taskList.indexOf(task)
                if (position != -1) {
                    performDeletion(position, task)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeletion(position: Int, task: Task) {
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        saveTasks()
        updateEmptyState()

        Snackbar.make(binding.root, "Task removed", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                taskList.add(position, task)
                taskAdapter.notifyItemInserted(position)
                saveTasks()
                updateEmptyState()
            }
            .setAnchorView(binding.fabAddTask)
            .show()
    }

    private fun updateEmptyState() {
        if (taskList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }
    }

    private fun saveTasks() {
        val json = gson.toJson(taskList)
        sharedPrefs.edit().putString("tasks_key", json).apply()
    }

    private fun loadTasks() {
        val json = sharedPrefs.getString("tasks_key", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            taskList = gson.fromJson(json, type)
        } else {
            // First run: Add sample tasks
            taskList = mutableListOf(
                Task(1, "Welcome to SyntecxHub To-Do!"),
                Task(2, "Swipe tasks to delete them"),
                Task(3, "Tap a task to mark it complete")
            )
        }
    }
}