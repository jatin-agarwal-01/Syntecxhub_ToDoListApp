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
import com.jatin.syntecxhub_todolist.databinding.FragmentSearchBinding
import com.jatin.syntecxhub_todolist.ui.home.HomeFragment
import com.jatin.syntecxhub_todolist.ui.home.TaskAdapter
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModel
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
                (parentFragment as? HomeFragment)?.showGuestLockedDialog()
                    ?: (activity?.supportFragmentManager
                        ?.findFragmentByTag("home") as? HomeFragment)?.showGuestLockedDialog()
                    ?: showLockDialog()
            }
            return
        }

        setupRecyclerView()
        setupSearch()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { task -> viewModel.updateTask(task) },
            onEdit   = { },
            onDelete = { task -> viewModel.deleteTask(task) }
        )
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    adapter.submitList(emptyList())
                    binding.tvResultCount.text = "Type to search your tasks"
                } else {
                    viewModel.search(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                adapter.submitList(results)
                binding.tvResultCount.text =
                    if (results.isEmpty()) "No tasks found"
                    else "${results.size} task${if (results.size > 1) "s" else ""} found"
            }
        }
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener    { filterByPriority(null) }
        binding.chipHigh.setOnClickListener   { filterByPriority(0) }
        binding.chipMedium.setOnClickListener { filterByPriority(1) }
        binding.chipLow.setOnClickListener    { filterByPriority(2) }
    }

    private fun filterByPriority(priority: Int?) {
        val all = viewModel.tasks.value
        val filtered = if (priority == null) all
                       else all.filter { it.priority == priority }
        adapter.submitList(filtered)
        binding.tvResultCount.text =
            "${filtered.size} task${if (filtered.size != 1) "s" else ""}"
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
