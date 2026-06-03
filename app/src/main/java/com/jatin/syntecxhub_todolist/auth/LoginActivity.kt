package com.jatin.syntecxhub_todolist.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jatin.syntecxhub_todolist.data.db.AppDatabase
import com.jatin.syntecxhub_todolist.data.repo.UserRepository
import com.jatin.syntecxhub_todolist.databinding.ActivityLoginBinding
import com.jatin.syntecxhub_todolist.ui.home.MainActivity
import com.jatin.syntecxhub_todolist.utils.SessionManager
import com.jatin.syntecxhub_todolist.utils.isValidEmail
import com.jatin.syntecxhub_todolist.utils.isValidPassword
import com.jatin.syntecxhub_todolist.utils.toast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(this)
        repo    = UserRepository(db.userDao())
        session = SessionManager(this)

        binding.btnLogin.setOnClickListener       { attemptLogin() }
        binding.btnGoogle.setOnClickListener      { loginWithGoogle() }
        binding.btnGuest.setOnClickListener       { continueAsGuest() }
        binding.tvRegister.setOnClickListener     {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            toast("Password recovery: check your registered email.")
        }
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!email.isValidEmail())    { binding.tilEmail.error    = "Enter a valid email";    return }
        if (!password.isValidPassword()) { binding.tilPassword.error = "Min 6 characters";      return }
        binding.tilEmail.error    = null
        binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val user = repo.login(email, password)
            if (user != null) {
                session.saveLogin(user.id, user.name, user.email)
                goToMain()
            } else {
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    toast("Invalid email or password")
                }
            }
        }
    }

    /**
     * Google Sign-In is simulated locally for the internship build.
     * In production you would integrate the actual Google Sign-In SDK here.
     */
    private fun loginWithGoogle() {
        toast("Google Sign-In: enter your details to register/login")
        startActivity(Intent(this, RegisterActivity::class.java).apply {
            putExtra("from_google", true)
        })
    }

    private fun continueAsGuest() {
        session.saveGuestSession()
        goToMain()
        toast("You're browsing as Guest. Some features are locked 🔒")
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
