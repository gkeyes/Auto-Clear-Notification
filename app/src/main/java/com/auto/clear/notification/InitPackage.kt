package com.auto.clear.notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class InitPackage : IXposedHookLoadPackage {
    companion object {
        const val TAG = "AutoClearNotification"
        val BLACKLIST = setOf(
            "android",
            "com.android.systemui",
            "com.miui.home",
            "com.android.launcher",
            "com.auto.clear.notification"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkgName = lpparam.packageName
        if (BLACKLIST.contains(pkgName)) return

        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onWindowFocusChanged",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val hasFocus = param.args[0] as Boolean
                        if (!hasFocus) return

                        val activity = param.thisObject as Activity
                        clearAppOwnNotifications(activity, pkgName)
                    }
                }
            )
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] Hook Activity 失败: ${e.message}")
        }
    }

    private fun clearAppOwnNotifications(context: Context, pkgName: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val activeNotifs = nm.activeNotifications
            if (activeNotifs.isNullOrEmpty()) return

            var clearedCount = 0
            for (notif in activeNotifs) {
                val isOngoing = (notif.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
                if (!isOngoing) {
                    nm.cancel(notif.tag, notif.id)
                    clearedCount++
                }
            }

            if (clearedCount > 0) {
                XposedBridge.log("[$TAG] [轻量级] $pkgName 获焦，已自动清理 $clearedCount 条通知")
            }
        } catch (e: Throwable) {
        }
    }
}
