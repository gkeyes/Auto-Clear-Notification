package com.auto.clear.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.auto.clear.notification.config.ConfigManager

class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConfigManager.init(this)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(ConfigManager.isEnabled) }
    var logs by remember { mutableStateOf("") }
    var logPath by remember { mutableStateOf("") }
    var isClearing by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NotifyDis",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "LSPosed 模块 v2.2",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "功能设置",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("启用通知清除")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            ConfigManager.isEnabled = it
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "说明: 打开 App 时自动清除该应用的通知。正在进行的通知会被保留。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "测试功能",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isClearing = true
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val activeNotifs = nm.activeNotifications
                        var cleared = 0
                        var skipped = 0
                        
                        for (notif in activeNotifs) {
                            try {
                                val isOngoing = (notif.notification.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0
                                if (isOngoing) {
                                    skipped++
                                    continue
                                }
                                nm.cancel(notif.tag, notif.id)
                                cleared++
                            } catch (e: Exception) {}
                        }
                        
                        Toast.makeText(context, "已清除 $cleared 条通知，跳过 $skipped 条进行中通知", Toast.LENGTH_LONG).show()
                        isClearing = false
                    },
                    enabled = !isClearing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isClearing) "清除中..." else "清除所有通知")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击后立即清除通知栏中的所有非进行中通知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "日志查看",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        OutlinedButton(onClick = {
                            logs = ModuleLogger.getLogs()
                            logPath = ModuleLogger.getLogFilePath() ?: "无"
                        }) {
                            Text("刷新")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            ModuleLogger.clear()
                            logs = ""
                        }) {
                            Text("清空")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "日志路径: $logPath",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = logs.ifEmpty { "点击「刷新」查看日志" },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "使用说明",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 在 LSPosed Manager 中启用本模块\n" +
                           "2. 在作用域中勾选「android」和「com.android.systemui」\n" +
                           "3. 重启设备生效\n" +
                           "4. 打开 App 时自动清除该应用的通知\n" +
                           "5. 正在进行的通知会被保留",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
