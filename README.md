# Auto-Clear-Notification

一款基于 LSPosed 框架的模块，用于解决「打开应用后通知不会自动从通知中心消失」的问题。

---

## 功能特性

- **Hook system_server 进程**：在 Activity 恢复/显示时自动清除通知
- **智能过滤**：自动排除正在进行的通知（如音乐播放、导航等）
- **用户可配置**：可自定义延迟时间、添加排除列表
- **系统应用保护**：内置白名单，自动排除系统应用

---

## 兼容性

| 项目 | 要求 |
|------|------|
| 目标系统 | 小米澎湃系统（HyperOS）/ MIUI |
| Android 版本 | Android 12 - Android 16 |
| 框架 | LSPosed / EdXposed |

---

## 技术原理

本模块 Hook `system_server` 进程中的 `ActivityRecord` 类：

### Hook 触发点

- `com.android.server.wm.ActivityRecord`
  - `performResume()` - Activity 恢复时
  - `showToUserIfNeeded()` - Activity 显示时

### 执行流程

1. 获取当前 Activity 的包名
2. 通过 `LocalServices` 获取 `NotificationManagerService` 实例
3. 调用 `cancelAllNotificationsInt()` 清除历史通知
4. 过滤正在进行的通知（`FLAG_ONGOING_EVENT`）

---

## 安装步骤

### 方式一：从源码编译

```bash
# 克隆项目
git clone https://github.com/gkeyes/Auto-Clear-Notification.git
cd Auto-Clear-Notification

# 编译 Debug 版本
./gradlew assembleDebug

# 安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 方式二：通过 LSPosed Manager

1. 在 LSPosed Manager 中搜索并启用本模块
2. 选择需要 Hook 的应用范围（通常选择「全部」）
3. 重启设备

---

## 使用说明

1. **启用模块**：在 LSPosed Manager 中启用并勾选作用域
2. **功能开关**：在模块设置中开启/关闭功能
3. **保留通知**：开启「保留正在进行的通知」保留音乐等通知
4. **清除延迟**：调整应用启动到清除通知的延迟时间
5. **排除应用**：在黑名单中添加不希望自动清除通知的应用

---

## 项目结构

```
Auto-Clear-Notification/
├── app/
│   ├── src/main/
│   │   ├── java/com/auto/clear/notification/
│   │   │   ├── InitPackage.kt      # LSPosed 入口类
│   │   │   ├── MainActivity.kt     # 主界面
│   │   │   ├── MainApplication.kt  # 应用入口
│   │   │   ├── config/
│   │   │   │   └── ConfigManager.kt    # 配置管理
│   │   │   ├── hook/
│   │   │   │   └── NotificationHooker.kt  # 核心 Hook 类
│   │   │   ├── ui/
│   │   │   │   └── SettingsActivity.kt # 设置界面
│   │   │   └── service/
│   │   │       └── ClearNotificationService.kt # 通知清除服务
│   │   ├── assets/
│   │   │   └── xposed_init       # Xposed 入口配置
│   │   └── AndroidManifest.xml  # 模块声明
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
└── README.md
```

---

## 注意事项

1. **需要 Root**：必须使用 Root 权限才能安装 LSPosed 模块
2. **重启生效**：安装或更新模块后需要重启设备
3. **权限要求**：模块不需要额外权限，通过系统 API 操作通知
4. **兼容性**：不同 MIUI/HyperOS 版本可能需要调整 Hook 点

---

## License

MIT License
