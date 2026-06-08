package com.jatin.syntecxhub_todolist.ui.stats

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.jatin.syntecxhub_todolist.R
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

                // Progress bar
                binding.progressCircle.progress = progress

                // Priority breakdown
                binding.tvHighCount.text   = high.toString()
                binding.tvMediumCount.text = medium.toString()
                binding.tvLowCount.text    = low.toString()

                val safeTotal = if (total > 0) total else 1
                binding.pbHigh.progress   = (high   * 100) / safeTotal
                binding.pbMedium.progress = (medium * 100) / safeTotal
                binding.pbLow.progress    = (low    * 100) / safeTotal

                // Category breakdown — build rows dynamically
                buildCategoryRows(tasks.groupBy { it.category }, total)

                // Motivational message
                binding.tvMotivation.text = when {
                    total == 0      -> "Add your first task to see stats! 📊"
                    progress == 100 -> "Perfect score! You're on fire 🔥"
                    progress >= 75  -> "Almost there — great work! 💪"
                    progress >= 50  -> "Halfway done — keep it up! 🚀"
                    else            -> "Every task completed is progress! ⭐"
                }
            }
        }
    }

    /**
     * Clears and rebuilds the dynamic category rows inside [layoutCategoryRows].
     * Each row shows: emoji+category name | progress bar | count badge
     */
    private fun buildCategoryRows(
        byCategory: Map<String, List<com.jatin.syntecxhub_todolist.data.model.Task>>,
        total: Int
    ) {
        val container = binding.layoutCategoryRows
        container.removeAllViews()

        if (byCategory.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text      = "No tasks yet"
                textSize  = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
            container.addView(tv)
            return
        }

        val ctx    = requireContext()
        val dp     = resources.displayMetrics.density
        val safeT  = if (total > 0) total else 1

        // Sort by count descending
        val sorted = byCategory.entries.sortedByDescending { it.value.size }

        val categoryEmojis = mapOf(
            "General"  to "📋",
            "Work"     to "💼",
            "Personal" to "🏠",
            "Shopping" to "🛒",
            "Health"   to "❤️",
            "Study"    to "📚",
            "Finance"  to "💰"
        )

        sorted.forEachIndexed { index, (category, taskList) ->
            val count      = taskList.size
            val doneCount  = taskList.count { it.isCompleted }
            val pct        = (count * 100) / safeT
            val emoji      = categoryEmojis[category] ?: "📁"

            // Row container
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { lp ->
                    if (index < sorted.size - 1) lp.bottomMargin = (12 * dp).toInt()
                }
            }

            // Category label
            val label = TextView(ctx).apply {
                text      = "$emoji $category"
                textSize  = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(
                    (0 * dp).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f
                )
            }

            // Progress bar
            val bar = ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
                max      = 100
                progress = pct
                layoutParams = LinearLayout.LayoutParams(
                    0, (8 * dp).toInt(), 2f
                ).also { lp ->
                    lp.marginStart = (8 * dp).toInt()
                    lp.marginEnd   = (8 * dp).toInt()
                }
            }

            // Count badge (e.g., "3/5")
            val badge = TextView(ctx).apply {
                text      = "$doneCount/$count"
                textSize  = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.purple_primary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.addView(label)
            row.addView(bar)
            row.addView(badge)
            container.addView(row)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
