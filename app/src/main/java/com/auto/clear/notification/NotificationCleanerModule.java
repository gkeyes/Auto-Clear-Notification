package com.auto.clear.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * Lightweight modern Xposed module:
 * when a scoped app gains window focus, clear that app's own non-ongoing notifications.
 */
public final class NotificationCleanerModule extends XposedModule {
    private static final String TAG = "AutoClearNotification";
    private static final AtomicBoolean HOOK_INSTALLED = new AtomicBoolean(false);
    private static final Set<String> PROCESS_BLACKLIST = Set.of(
            "android",
            "com.android.systemui",
            "com.auto.clear.notification"
    );

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        logInfo("loaded in process: " + safeProcessName(param));
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        String packageName = param.getPackageName();
        String processName = safeProcessName(param);
        if (packageName == null || processName == null) {
            return;
        }
        if (PROCESS_BLACKLIST.contains(packageName) || !packageName.equals(processName)) {
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
            logError("failed to hook focus callback", t);
        }
    }

    private void clearOwnNotifications(Activity activity, String packageName) {
        try {
            NotificationManager nm =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                return;
            }

            var activeNotifications = nm.getActiveNotifications();
            if (activeNotifications == null || activeNotifications.length == 0) {
                return;
            }

            int cleared = 0;
            for (var sbn : activeNotifications) {
                Notification notification = sbn.getNotification();
                if (notification == null) {
                    continue;
                }
                boolean ongoing = (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;
                if (ongoing) {
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

    private String safeProcessName(XposedModuleInterface.ModuleLoadedParam param) {
        try {
            return param.getProcessName();
        } catch (Throwable ignored) {
            return "<unknown>";
        }
    }

    private String safeProcessName(XposedModuleInterface.PackageLoadedParam param) {
        try {
            return param.getProcessName();
        } catch (Throwable ignored) {
            return "<unknown>";
        }
    }

    private void logInfo(String message) {
        log(Log.INFO, TAG, message);
    }

    private void logError(String message, Throwable throwable) {
        log(Log.ERROR, TAG, message, throwable);
    }
}
