package com.jatin.syntecxhub_todolist

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jatin.syntecxhub_todolist.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private var selectedDueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatabase()
        setupViewModel()
        setupRecyclerView()
        setupFAB()
        setupSwipeToDelete()
        observeTasks()
    }

    private fun setupDatabase() {
        val db = TaskDatabase.getDatabase(applicationContext)
        val dao = db.taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    private fun setupViewModel() {
        // Initialized in setupDatabase
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onComplete = { task ->
                viewModel.updateTask(task)
                showSnackbar("Task marked ${if (task.isCompleted) "complete" else "incomplete"}")
            },
            onEdit = { task ->
                showEditDialog(task)
            },
            onDelete = { task ->
                viewModel.deleteTask(task)
                showSnackbar("Task deleted", undoTask = task)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position >= 0) {
                    val task = adapter.currentList[position]
                    viewModel.deleteTask(task)
                    showSnackbar("Task deleted", undoTask = task)
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerView)
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            viewModel.taskList.collect { tasks ->
                adapter.submitList(tasks)
                binding.emptyStateText.visibility = if (tasks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextDescription)

        selectedDueDate = null

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    val desc = descInput.text.toString().trim()
                    viewModel.addTask(title, desc, 1, selectedDueDate)
                    showSnackbar("Task added successfully")
                } else {
                    showSnackbar("Please enter a task title")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextDescription)

        titleInput.setText(task.title)
        descInput.setText(task.description)
        selectedDueDate = task.dueDate

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val title = titleInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    val desc = descInput.text.toString().trim()
                    val updatedTask = task.copy(
                        title = title,
                        description = desc,
                        dueDate = selectedDueDate
                    )
                    viewModel.updateTask(updatedTask)
                    showSnackbar("Task updated")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSnackbar(message: String, undoTask: Task? = null) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            if (undoTask != null) {
                setAction("UNDO") {
                    viewModel.addTask(undoTask.title, undoTask.description, undoTask.priority, undoTask.dueDate)
                }
            }
            show()
        }
    }
}
