package com.auto.clear.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * Lightweight modern Xposed (API 101) module.
 *
 * <p>When an activity of a scoped app gains window focus, cancel that app's own
 * non-ongoing notifications. Works only inside the target app process; it never
 * touches system_server or other apps' notifications.
 */
public final class NotificationCleanerModule extends XposedModule {
    private static final String TAG = "AutoClearNotification";

    /** The hook is installed at most once per process. */
    private static final AtomicBoolean HOOK_INSTALLED = new AtomicBoolean(false);

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        logInfo("loaded in process: " + safeProcessName(param));
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        String packageName = param.getPackageName();
        if (isIgnoredPackage(packageName) || !param.isFirstPackage()) {
            return;
        }
        if (!HOOK_INSTALLED.compareAndSet(false, true)) {
            return;
        }

        try {
            Method focusMethod = Activity.class.getDeclaredMethod("onWindowFocusChanged", boolean.class);
            focusMethod.setAccessible(true);
            hook(focusMethod)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        // Let the original callback run first, then clean up.
                        Object result = chain.proceed();
                        boolean hasFocus = Boolean.TRUE.equals(chain.getArg(0));
                        Object thisObject = chain.getThisObject();
                        if (hasFocus && thisObject instanceof Activity) {
                            clearOwnNotifications((Activity) thisObject, packageName);
                        }
                        return result;
                    });
            logInfo("hooked Activity.onWindowFocusChanged for " + packageName);
        } catch (Throwable t) {
            logError("failed to hook focus callback for " + packageName, t);
        }
    }

    /** Cancels every active, non-ongoing notification posted by this app. */
    private void clearOwnNotifications(Activity activity, String packageName) {
        try {
            Object service = activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!(service instanceof NotificationManager)) {
                return;
            }
            NotificationManager nm = (NotificationManager) service;

            StatusBarNotification[] activeNotifications = nm.getActiveNotifications();
            if (activeNotifications == null || activeNotifications.length == 0) {
                return;
            }

            int cleared = 0;
            for (StatusBarNotification sbn : activeNotifications) {
                if (sbn == null || isOngoing(sbn.getNotification())) {
                    continue;
                }
                nm.cancel(sbn.getTag(), sbn.getId());
                cleared++;
            }

            if (cleared > 0) {
                logInfo("cleared " + cleared + " notifications for " + packageName);
            }
        } catch (Throwable t) {
            logError("clear notifications failed for " + packageName, t);
        }
    }

    /** A missing notification is treated as ongoing so it is left alone. */
    private static boolean isOngoing(Notification notification) {
        return notification == null
                || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;
    }

    private static String safeProcessName(XposedModuleInterface.ModuleLoadedParam param) {
        try {
            return param.getProcessName();
        } catch (Throwable ignored) {
            return "<unknown>";
        }
    }

    /** Packages the hook must never run in, even if they end up in scope. */
    private static boolean isIgnoredPackage(String packageName) {
        return "android".equals(packageName)
                || "com.android.systemui".equals(packageName)
                || "com.auto.clear.notification".equals(packageName);
    }

    private void logInfo(String message) {
        log(Log.INFO, TAG, message);
    }

    private void logError(String message, Throwable throwable) {
        log(Log.ERROR, TAG, message, throwable);
    }
}
