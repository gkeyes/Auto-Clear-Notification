package com.auto.clear.notification

import android.app.Application
import com.auto.clear.notification.config.ConfigManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ModuleLogger.init(this)
        ModuleLogger.d("App", "模块应用启动")
        ConfigManager.init(this)
    }
}
