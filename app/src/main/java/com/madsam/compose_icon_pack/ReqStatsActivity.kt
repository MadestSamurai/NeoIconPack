/*
 * Copyright 2017 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.madsam.compose_icon_pack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.madsam.compose_icon_pack.screens.ReqStatsScreen
import com.madsam.compose_icon_pack.ui.theme.ComposeIconPackTheme
import com.madsam.compose_icon_pack.util.AppPreferences
import kotlinx.coroutines.launch

class ReqStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeIconPackTheme {
                val context = LocalContext.current
                val preferences = remember { AppPreferences(context) }
                val coroutineScope = rememberCoroutineScope()

                // 从DataStore获取用户名，使用Flow收集状态
                val username by preferences.getString("user").collectAsState(initial = "")
                var showSignInDialog by remember { mutableStateOf(username.isEmpty()) }

                // 登录对话框
                if (showSignInDialog) {
                    UserSignInDialog(
                        onContinue = { user ->
                            if (user.isNotEmpty()) {
                                coroutineScope.launch {
                                    preferences.saveString("user", user)
                                    showSignInDialog = false
                                }
                            }
                        },
                        onDismiss = {
                            if (username.isEmpty()) {
                                // 如果没有用户名则退出
                                finish()
                            } else {
                                showSignInDialog = false
                            }
                        }
                    )
                }

                // 主界面
                ReqStatsActivityScreen(
                    username = username,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReqStatsActivityScreen(
    username: String,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图标申请统计") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (username.isNotEmpty()) {
            // 用户已登录，显示请求统计页面
            ReqStatsScreen(
                username = username,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // 未登录状态（通常不会到达这里，因为对话框会阻止）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("请先登录")
            }
        }
    }
}

@Composable
fun UserSignInDialog(
    onContinue: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("请输入用户名") },
        text = {
            Column {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        showError = false
                    },
                    label = { Text("用户名") },
                    isError = showError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                if (showError) {
                    Text(
                        text = "用户名不能为空",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = "请输入您的用户名以查看图标申请统计。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userInput.trim().isEmpty()) {
                        showError = true
                    } else {
                        onContinue(userInput.trim())
                    }
                }
            ) {
                Text("继续")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}