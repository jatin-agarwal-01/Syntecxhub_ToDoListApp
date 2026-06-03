package com.jatin.syntecxhub_todolist.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jatin.syntecxhub_todolist.databinding.FragmentProfileBinding
import com.jatin.syntecxhub_todolist.ui.home.MainActivity
import com.jatin.syntecxhub_todolist.ui.home.TaskViewModel
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.gone
import com.jatin.syntecxhub_todolist.utils.toast
import com.jatin.syntecxhub_todolist.utils.visible
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

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

        setupProfile()
        setupDarkMode()
        setupStats()
        setupLogout()
    }

    private fun setupProfile() {
        binding.tvName.text    = session.getUserName()
        binding.tvEmail.text   = session.getUserEmail()
        binding.tvInitials.text = session.getUserName()
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.isChecked = session.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            toast(if (isChecked) "Dark mode on 🌙" else "Light mode on ☀️")
        }
    }

    private fun setupStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { tasks ->
                val total     = tasks.size
                val completed = tasks.count { it.isCompleted }
                binding.tvProfileTotal.text     = total.toString()
                binding.tvProfileCompleted.text = completed.toString()
                binding.tvProfilePending.text   = (total - completed).toString()
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    (requireActivity() as MainActivity).logout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
