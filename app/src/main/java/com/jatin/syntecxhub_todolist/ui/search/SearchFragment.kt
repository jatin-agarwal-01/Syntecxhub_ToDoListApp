package com.jatin.syntecxhub_todolist.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.jatin.syntecxhub_todolist.R
import com.jatin.syntecxhub_todolist.databinding.FragmentSearchBinding
import com.jatin.syntecxhub_todolist.ui.home.TaskAdapter
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModel
import com.jatin.syntecxhub_todolist.ui.task.AddEditTaskActivity
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter
    private lateinit var session: SessionManager

    /** Null = show all priorities; 0/1/2 = High/Med/Low filter */
    private var activePriority: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        // Guest lock
        if (session.isGuest()) {
            binding.layoutLocked.visible()
            binding.layoutContent.gone()
            binding.btnUnlock.setOnClickListener {
                startActivity(android.content.Intent(
                    requireContext(),
                    com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java
                ))
            }
            return
        }

        setupRecyclerView()
        setupSearch()
        setupFilters()
        // Show all tasks on first open
        applyFilter()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { task -> viewModel.updateTask(task) },
            onEdit   = { task -> AddEditTaskActivity.startForEdit(requireContext(), task) },
            onDelete = { task -> viewModel.deleteTask(task) }
        )
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter       = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observe all tasks to re-apply filter when underlying data changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { applyFilter() }
        }
    }

    private fun setupFilters() {
        binding.chipGroupPriority.setOnCheckedStateChangeListener { group, _ ->
            activePriority = when (group.checkedChipId) {
                R.id.chipHigh   -> 0
                R.id.chipMedium -> 1
                R.id.chipLow    -> 2
                else            -> null   // chipAll or none
            }
            applyFilter()
        }
    }

    /**
     * Combined filter: search text AND/OR priority chip.
     * - No text + All chip → show all tasks
     * - Text only          → show tasks matching text (title or description)
     * - Priority chip only → show tasks of that priority
     * - Both               → intersection
     */
    private fun applyFilter() {
        val query = binding.etSearch.text?.toString()?.trim() ?: ""
        var results = viewModel.tasks.value

        // Apply text filter
        if (query.isNotEmpty()) {
            val lower = query.lowercase()
            results = results.filter {
                it.title.lowercase().contains(lower) ||
                it.description.lowercase().contains(lower)
            }
        }

        // Apply priority filter
        val p = activePriority
        if (p != null) {
            results = results.filter { it.priority == p }
        }

        adapter.submitList(results)

        binding.tvResultCount.text = when {
            query.isEmpty() && activePriority == null ->
                "${results.size} task${if (results.size != 1) "s" else ""} total"
            results.isEmpty() -> "No tasks found"
            else -> "${results.size} task${if (results.size != 1) "s" else ""} found"
        }
    }

    private fun showLockDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔒 Feature Locked")
            .setMessage("Search & Filter is available for registered users only.")
            .setPositiveButton("Login / Register") { _, _ ->
                startActivity(android.content.Intent(
                    requireContext(),
                    com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java
                ))
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
