package com.auto.clear.notification.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.auto.clear.notification.config.ConfigManager

class ClearNotificationService : NotificationListenerService() {
    companion object {
        private const val TAG = "ClearNotifService"
        private val WHITELIST = setOf(
            "com.android.systemui",
            "com.android.launcher",
            "com.miui.home",
            "com.miui.securitycenter",
            "com.android.settings",
            "com.android.phone",
            "com.android.contacts",
            "com.auto.clear.notification",
            "com.xiaomi.xmsf",
            "com.miui.packageinstaller",
            "com.xiaomi.market"
        )
        private val lastClearTime = mutableMapOf<String, Long>()
        private const val CLEAR_INTERVAL = 500L
        private fun log(msg: String) = Log.i(TAG, msg)
    }

    override fun onCreate() {
        super.onCreate()
        log("ClearNotificationService 已创建")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("ClearNotificationService 已销毁")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: android.service.notification.NotificationListenerService.RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)
        if (!ConfigManager.isEnabled) return
        sbn ?: return
        val pkg = sbn.packageName ?: return
        if (WHITELIST.contains(pkg)) return
        val notif = sbn.notification ?: return
        val isOngoing = (notif.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isLocalOnly = (notif.flags and Notification.FLAG_LOCAL_ONLY) != 0
        log("收到通知 pkg=$pkg, ongoing=$isOngoing, local=$isLocalOnly, key=${sbn.key}")
        if (!isOngoing && !isLocalOnly) {
            scheduleClearNotification(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: android.service.notification.NotificationListenerService.RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        sbn ?: return
        log("移除通知 pkg=${sbn.packageName}, reason=$reason")
    }

    private fun scheduleClearNotification(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        val now = System.currentTimeMillis()
        val lastTime = lastClearTime[pkg] ?: 0
        if (now - lastTime < CLEAR_INTERVAL) {
            log("跳过清除（过于频繁）pkg=$pkg")
            return
        }
        lastClearTime[pkg] = now
        Thread {
            try {
                Thread.sleep(ConfigManager.delayMs)
                clearNotification(sbn)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log("清除异常: ${e.message}")
            }
        }.start()
    }

    private fun clearNotification(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        val key = sbn.key
        try {
            cancelNotification(key)
            log("✅ 清除成功 pkg=$pkg, key=$key")
        } catch (e: Exception) {
            log("❌ 清除失败: ${e.message}")
        }
    }

    fun clearAllNonOngoing() {
        try {
            val activeNotifs = activeNotifications
            var cleared = 0
            for (sbn in activeNotifs) {
                try {
                    val pkg = sbn.packageName
                    if (WHITELIST.contains(pkg)) continue
                    val isOngoing = (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
                    if (isOngoing) continue
                    cancelNotification(sbn.key)
                    cleared++
                } catch (e: Exception) {
                    log("清除单条异常: ${e.message}")
                }
            }
            log("批量清除完成: $cleared 条")
        } catch (e: Exception) {
            log("批量清除异常: ${e.message}")
        }
    }
}
