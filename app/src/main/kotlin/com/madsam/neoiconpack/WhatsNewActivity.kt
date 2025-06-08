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

package com.madsam.neoiconpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.madsam.neoiconpack.bean.IconBean
import com.madsam.neoiconpack.screens.IconGridScreen
import com.madsam.neoiconpack.ui.theme.ComposeIconPackTheme
import com.madsam.neoiconpack.util.LatestIconsGetter
import com.madsam.neoiconpack.util.PkgUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WhatsNewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gridItemMode = resources.getInteger(R.integer.whats_new_grid_item_mode)

        setContent {
            ComposeIconPackTheme {
                WhatsNewScreen(
                    gridItemMode = gridItemMode,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewScreen(
    gridItemMode: Int,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var icons by remember { mutableStateOf<List<IconBean>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 加载最新图标
    LaunchedEffect(true) {
        try {
            LatestIconsGetter().getIcons(context).let { iconList ->
                icons = iconList
            }
        } finally {
            isLoading = false

            // 延迟显示提示
            delay(400)
            val message = PkgUtil.getAppVer(context, context.getString(R.string.toast_whats_new))
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "新增") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { snackbarData ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    snackbarData = snackbarData
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 最新图标网格
            IconGridScreen(
                icons = icons,
                isLoading = isLoading,
                gridItemMode = gridItemMode,
                paddingValues = paddingValues,
                onItemClick = { icon ->
                    // 显示图标详情
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("已选择: ${icon.label ?: icon.name}")
                    }
                }
            )
        }
    }
}