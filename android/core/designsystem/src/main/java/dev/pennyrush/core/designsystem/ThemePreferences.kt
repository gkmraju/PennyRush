package dev.pennyrush.core.designsystem

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { System, Light, Dark }

object ThemePreferences {
    private const val PREFS = "pennyrush_prefs"
    private const val KEY_THEME = "theme_mode"

    private var prefs: SharedPreferences? = null

    var themeMode: ThemeMode by mutableStateOf(ThemeMode.System)
        private set

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        themeMode = runCatching {
            ThemeMode.valueOf(p.getString(KEY_THEME, null) ?: ThemeMode.System.name)
        }.getOrDefault(ThemeMode.System)
    }

    fun set(mode: ThemeMode) {
        themeMode = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }
}
