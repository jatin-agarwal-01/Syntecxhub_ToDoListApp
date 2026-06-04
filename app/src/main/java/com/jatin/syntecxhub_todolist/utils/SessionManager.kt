package com.jatin.syntecxhub_todolist.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("todo_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID     = "user_id"
        private const val KEY_USER_NAME   = "user_name"
        private const val KEY_USER_EMAIL  = "user_email"
        private const val KEY_IS_GUEST    = "is_guest"
        private const val KEY_DARK_MODE   = "dark_mode"
        private const val KEY_USE_24HR    = "use_24hr"
        const val GUEST_ID                = -1
    }

    /* ── Save session ─────────────────────────────────────── */

    fun saveLogin(userId: Int, name: String, email: String) {
        prefs.edit()
            .putInt(KEY_USER_ID,       userId)
            .putString(KEY_USER_NAME,  name)
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_GUEST,  false)
            .apply()
    }

    fun saveGuestSession() {
        prefs.edit()
            .putInt(KEY_USER_ID,       GUEST_ID)
            .putString(KEY_USER_NAME,  "Guest")
            .putString(KEY_USER_EMAIL, "")
            .putBoolean(KEY_IS_GUEST,  true)
            .apply()
    }

    /* ── Read session ─────────────────────────────────────── */

    fun isLoggedIn(): Boolean  = prefs.getInt(KEY_USER_ID, Int.MIN_VALUE) != Int.MIN_VALUE
    fun isGuest(): Boolean     = prefs.getBoolean(KEY_IS_GUEST, false)
    fun getUserId(): Int       = prefs.getInt(KEY_USER_ID, GUEST_ID)
    fun getUserName(): String  = prefs.getString(KEY_USER_NAME,  "Guest") ?: "Guest"
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "")      ?: ""

    /* ── Dark mode ────────────────────────────────────────── */

    fun setDarkMode(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    fun isDarkMode(): Boolean =
        prefs.getBoolean(KEY_DARK_MODE, false)

    /* ── Time format ──────────────────────────────────────── */

    fun setUse24Hr(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_USE_24HR, enabled).apply()
    fun isUse24Hr(): Boolean =
        prefs.getBoolean(KEY_USE_24HR, false)   // default = 12-hour

    /* ── Logout ───────────────────────────────────────────── */

    fun logout() = prefs.edit().clear().apply()
}