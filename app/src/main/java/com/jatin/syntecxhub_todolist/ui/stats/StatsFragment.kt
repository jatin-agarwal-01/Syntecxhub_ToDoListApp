package com.jatin.syntecxhub_todolist.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.jatin.syntecxhub_todolist.databinding.FragmentStatsBinding
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModel
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())

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

        observeStats()
    }

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { tasks ->
                val total     = tasks.size
                val completed = tasks.count { it.isCompleted }
                val pending   = total - completed
                val high      = tasks.count { it.priority == 0 }
                val medium    = tasks.count { it.priority == 1 }
                val low       = tasks.count { it.priority == 2 }
                val progress  = if (total > 0) (completed * 100) / total else 0

                // Summary numbers
                binding.tvTotalValue.text     = total.toString()
                binding.tvCompletedValue.text = completed.toString()
                binding.tvPendingValue.text   = pending.toString()
                binding.tvProgressValue.text  = "$progress%"

                // Progress ring
                binding.progressCircle.progress = progress

                // Priority breakdown
                binding.tvHighCount.text    = high.toString()
                binding.tvMediumCount.text  = medium.toString()
                binding.tvLowCount.text     = low.toString()

                // Progress bars for priority
                val safeTotal = if (total > 0) total else 1
                binding.pbHigh.progress   = (high   * 100) / safeTotal
                binding.pbMedium.progress = (medium * 100) / safeTotal
                binding.pbLow.progress    = (low    * 100) / safeTotal

                // Category breakdown
                val byCategory = tasks.groupBy { it.category }
                    .map { (cat, list) -> "$cat  (${list.size})" }
                    .joinToString("\n")
                binding.tvCategoryBreakdown.text =
                    if (byCategory.isEmpty()) "No tasks yet" else byCategory

                // Motivational message
                binding.tvMotivation.text = when {
                    total == 0    -> "Add your first task to see stats! 📊"
                    progress == 100 -> "Perfect score! You're on fire 🔥"
                    progress >= 75 -> "Almost there — great work! 💪"
                    progress >= 50 -> "Halfway done — keep it up! 🚀"
                    else           -> "Every task completed is progress! ⭐"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
