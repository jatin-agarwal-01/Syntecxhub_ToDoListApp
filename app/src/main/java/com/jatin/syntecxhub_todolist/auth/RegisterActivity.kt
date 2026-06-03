package com.jatin.syntecxhub_todolist.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jatin.syntecxhub_todolist.data.db.AppDatabase
import com.jatin.syntecxhub_todolist.data.model.User
import com.jatin.syntecxhub_todolist.data.repo.UserRepository
import com.jatin.syntecxhub_todolist.databinding.ActivityRegisterBinding
import com.jatin.syntecxhub_todolist.ui.home.MainActivity
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.isValidEmail
import com.jatin.syntecxhub_todolist.utils.isValidPassword
import com.jatin.syntecxhub_todolist.utils.toast
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(this)
        repo    = UserRepository(db.userDao())
        session = SessionManager(this)

        // Pre-fill name if coming from Google sign-in simulation
        if (intent.getBooleanExtra("from_google", false)) {
            binding.tvTitle.text    = "Complete Google Sign-In"
            binding.tvSubtitle.text = "Just fill in your details below"
        }

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptRegister() {
        val name     = binding.etName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm  = binding.etConfirmPassword.text.toString().trim()

        // Validate
        var valid = true
        if (name.length < 2) {
            binding.tilName.error = "Enter your full name"; valid = false
        } else binding.tilName.error = null

        if (!email.isValidEmail()) {
            binding.tilEmail.error = "Enter a valid email"; valid = false
        } else binding.tilEmail.error = null

        if (!password.isValidPassword()) {
            binding.tilPassword.error = "Min 6 characters"; valid = false
        } else binding.tilPassword.error = null

        if (confirm != password) {
            binding.tilConfirm.error = "Passwords don't match"; valid = false
        } else binding.tilConfirm.error = null

        if (!valid) return

        binding.btnRegister.isEnabled = false
        lifecycleScope.launch {
            // Check if email already registered
            val existing = repo.getUserByEmail(email)
            if (existing != null) {
                runOnUiThread {
                    binding.btnRegister.isEnabled = true
                    binding.tilEmail.error = "Email already registered"
                }
                return@launch
            }

            val newId = repo.register(
                User(name = name, email = email, password = password)
            )
            session.saveLogin(newId.toInt(), name, email)
            runOnUiThread {
                toast("Welcome, $name! 🎉")
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
