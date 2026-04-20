package com.auto.clear.notification

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object ModuleLogger {
    private const val TAG = "AutoClearNotification"
    private const val LOG_FILE = "module_log.txt"
    private const val MAX_LINES = 500
    private val logQueue = ConcurrentLinkedQueue<String>()
    private var logFile: File? = null
    
    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }
    
    fun init(context: Context) {
        try {
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            logFile = File(logDir, LOG_FILE)
            loadHistory()
            i("========== 模块启动 ==========")
            i("版本: 2.0")
            i("设备: ${android.os.Build.MODEL}")
            i("SDK: ${android.os.Build.VERSION.SDK_INT}")
            i("================================")
        } catch (e: Throwable) {
            Log.e(TAG, "日志初始化失败: ${e.message}")
        }
    }
    
    private fun loadHistory() {
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    file.readLines().takeLast(MAX_LINES / 2).forEach { line ->
                        logQueue.offer(line)
                    }
                }
            }
        } catch (e: Throwable) {}
    }
    
    fun log(level: Level, tag: String, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val levelStr = when (level) {
            Level.DEBUG -> "D"
            Level.INFO -> "I"
            Level.WARN -> "W"
            Level.ERROR -> "E"
        }
        val logLine = "$timestamp $levelStr [$tag] $message"
        logQueue.offer(logLine)
        while (logQueue.size > MAX_LINES) {
            logQueue.poll()
        }
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine(logLine)
                }
            }
        } catch (e: Throwable) {}
        when (level) {
            Level.DEBUG -> Log.d(tag, message)
            Level.INFO -> Log.i(tag, message)
            Level.WARN -> Log.w(tag, message)
            Level.ERROR -> Log.e(tag, message)
        }
    }
    
    fun getLogs(): String = logQueue.joinToString("\n")
    fun getLogFilePath(): String? = logFile?.absolutePath
    fun clear() {
        logQueue.clear()
        try { logFile?.delete() } catch (e: Throwable) {}
    }
    
    fun d(tag: String, msg: String) = log(Level.DEBUG, tag, msg)
    fun i(msg: String) = log(Level.INFO, "App", msg)
    fun w(tag: String, msg: String) = log(Level.WARN, tag, msg)
    fun e(tag: String, msg: String) = log(Level.ERROR, tag, msg)
}
