# Auto-Clear-Notification

轻量型 AP101 Xposed 模块：当被作用域选中的 App 重新获得窗口焦点时，自动清理该 App 自己发出的、非持续性的通知。

## 定位

- 不 hook `system_server`
- 不操作 `NotificationManagerService`
- 不尝试清理其他应用的通知
- 只在目标 App 进程内工作

## 当前行为

模块会在被选中 App 的 `Activity.onWindowFocusChanged(true)` 之后执行：

1. 获取该 App 自己当前仍然活跃的通知
2. 跳过 ongoing 通知
3. 清除其余通知

这和 Android `NotificationManager.getActiveNotifications()` 的能力边界一致。

## 技术栈

- modern `libxposed` API `101`
- Android Gradle Plugin `8.2.0`
- 原生 Android Activity 作为说明页 UI
- release APK 使用 CI debug signing key 签名

## 项目结构

```text
app/src/main/java/com/auto/clear/notification/
├── MainActivity.java
└── NotificationCleanerModule.java

app/src/main/resources/META-INF/xposed/
├── java_init.list
└── module.prop

.github/workflows/
└── android-release.yml
```

## 本地编译

```bash
./gradlew assembleRelease
```

如果你更习惯 Android Studio，直接导入工程后执行 `assembleRelease` 即可。

## GitHub Actions

仓库包含 release 构建工作流：

- 文件：`.github/workflows/android-release.yml`
- 输出：`app/build/outputs/apk/release/*.apk`
- Artifact 名称：`Auto-Clear-Notification-release-apk`
- APK 使用 GitHub runner 的 debug signing key 签名，可直接安装。
- 如果需要长期覆盖安装兼容，应该改用 GitHub Secrets 注入私有 release keystore。

## 使用方式

1. 安装 APK
2. 在 LSPosed 或 Vector 中启用模块
3. 只给你希望自动清理通知的 App 勾选作用域
4. 重新打开目标 App
