package com.auto.clear.notification.config

import android.content.Context
import android.content.SharedPreferences

object ConfigManager {
    private const val KEY_ENABLED = "enabled"
    private const val KEY_EXCLUDE_ONGOING = "exclude_ongoing"
    private const val KEY_DELAY_MS = "delay_ms"
    private const val KEY_BLACKLIST = "blacklist"
    private const val DEFAULT_DELAY_MS = 500L
    private const val DEFAULT_BLACKLIST = ""
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("auto_clear_notification_prefs", Context.MODE_PRIVATE)
        syncToHookModule()
    }
    
    private fun syncToHookModule() {
        try {
            val hookClass = Class.forName("com.auto.clear.notification.InitPackage")
            val enabledField = hookClass.getDeclaredField("enabled")
            val excludeOngoingField = hookClass.getDeclaredField("excludeOngoing")
            val delayMsField = hookClass.getDeclaredField("delayMs")
            enabledField.set(null, isEnabled)
            excludeOngoingField.set(null, excludeOngoing)
            delayMsField.set(null, delayMs)
        } catch (e: Throwable) {}
    }
    
    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
            syncToHookModule()
        }
    
    var excludeOngoing: Boolean
        get() = prefs.getBoolean(KEY_EXCLUDE_ONGOING, true)
        set(value) {
            prefs.edit().putBoolean(KEY_EXCLUDE_ONGOING, value).apply()
            syncToHookModule()
        }
    
    var delayMs: Long
        get() = prefs.getLong(KEY_DELAY_MS, DEFAULT_DELAY_MS)
        set(value) {
            prefs.edit().putLong(KEY_DELAY_MS, value).apply()
            syncToHookModule()
        }
    
    var blacklist: String
        get() = prefs.getString(KEY_BLACKLIST, DEFAULT_BLACKLIST) ?: DEFAULT_BLACKLIST
        set(value) {
            prefs.edit().putString(KEY_BLACKLIST, value).apply()
        }
    
    fun getBlacklistPackages(): Set<String> {
        return blacklist.split("\n").filter { it.isNotBlank() }.toSet()
    }
    
    fun addBlacklistPackage(packageName: String) {
        val current = blacklist
        val newList = if (current.isBlank()) packageName else "$current\n$packageName"
        blacklist = newList
    }
    
    fun removeBlacklistPackage(packageName: String) {
        val packages = blacklist.split("\n").filter { it.isNotBlank() && it != packageName }
        blacklist = packages.joinToString("\n")
    }
}
