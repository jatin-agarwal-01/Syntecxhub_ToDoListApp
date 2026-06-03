package com.jatin.syntecxhub_todolist.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.jatin.syntecxhub_todolist.R
import com.jatin.syntecxhub_todolist.databinding.ActivityMainBinding
import com.jatin.syntecxhub_todolist.ui.profile.ProfileFragment
import com.jatin.syntecxhub_todolist.ui.search.SearchFragment
import com.jatin.syntecxhub_todolist.ui.stats.StatsFragment
import com.jatin.syntecxhub_todolist.utils.NotificationHelper
import com.jatin.syntecxhub_todolist.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark mode before super.onCreate
        session = SessionManager(this)
        AppCompatDelegate.setDefaultNightMode(
            if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannel(this)

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { loadFragment(HomeFragment());    true }
                R.id.nav_search  -> { loadFragment(SearchFragment());  true }
                R.id.nav_stats   -> { loadFragment(StatsFragment());   true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else             -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Called by ProfileFragment when user logs out. */
    fun logout() {
        session.logout()
        val intent = Intent(
            this,
            com.jatin.syntecxhub_todolist.auth.LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
