package com.jatin.syntecxhub_todolist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jatin.syntecxhub_todolist.databinding.ActivityMainBinding

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

        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            taskList,
            onTaskChecked = { saveTasks() },
            onEditClicked = { task -> showTaskDialog(task) },
            onDeleteClicked = { task -> showDeleteConfirmationDialog(task) }
        )
        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        setupSwipeToDelete()
        updateEmptyState()
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                performDeletion(position)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvTasks)
    }

    private fun performDeletion(position: Int) {
        val deletedTask = taskList[position]
        val deletedPosition = position

        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        saveTasks()
        updateEmptyState()

        Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                taskList.add(deletedPosition, deletedTask)
                taskAdapter.notifyItemInserted(deletedPosition)
                saveTasks()
                updateEmptyState()
            }.show()
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                val index = taskList.indexOf(task)
                if (index != -1) {
                    performDeletion(index)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaskDialog(taskToEdit: Task? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null)
        val etTaskTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)

        val title = if (taskToEdit == null) "Add Task" else "Edit Task"
        if (taskToEdit != null) {
            etTaskTitle.setText(taskToEdit.title)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val taskTitle = etTaskTitle.text.toString().trim()
                if (taskTitle.isNotEmpty()) {
                    if (taskToEdit == null) {
                        // Add new task
                        val newTask = Task(System.currentTimeMillis(), taskTitle, false)
                        taskList.add(newTask)
                        taskAdapter.notifyItemInserted(taskList.size - 1)
                    } else {
                        // Update existing task
                        val index = taskList.indexOf(taskToEdit)
                        if (index != -1) {
                            taskList[index] = taskToEdit.copy(title = taskTitle)
                            taskAdapter.notifyItemChanged(index)
                        }
                    }
                    saveTasks()
                    updateEmptyState()
                } else {
                    Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmptyState() {
        if (taskList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }
    }

    private fun saveTasks() {
        val json = gson.toJson(taskList)
        sharedPrefs.edit().putString("tasks", json).apply()
    }

    private fun loadTasks() {
        val json = sharedPrefs.getString("tasks", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            taskList = gson.fromJson(json, type)
        } else {
            // Sample tasks for first run
            taskList = mutableListOf(
                Task(1, "Welcome to ToDo List!", false),
                Task(2, "Click FAB to add tasks", false),
                Task(3, "Swipe or click delete to remove", false)
            )
        }
    }
}
