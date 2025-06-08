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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.madsam.neoiconpack.bean.IconBean
import com.madsam.neoiconpack.screens.IconGridScreen
import com.madsam.neoiconpack.ui.theme.ComposeIconPackTheme
import com.madsam.neoiconpack.util.AllIconsGetter

class IconPickActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickerGridItemMode = resources.getInteger(R.integer.picker_grid_item_mode)

        setContent {
            ComposeIconPackTheme {
                IconPickScreen(
                    pickerGridItemMode = pickerGridItemMode,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickScreen(
    pickerGridItemMode: Int,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var icons by remember { mutableStateOf<List<IconBean>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 加载图标数据
    LaunchedEffect(true) {
        try {
            AllIconsGetter().getIcons(context).let { iconList ->
                icons = iconList
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "选择图标") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // 图标网格展示
            IconGridScreen(
                icons = icons,
                isLoading = isLoading,
                gridItemMode = pickerGridItemMode,
                paddingValues = paddingValues,
                onItemClick = { icon ->
                    // 这里处理图标选择逻辑
                    // 例如设置结果并完成活动
                    // setResult(...)
                    onBackPressed()
                }
            )
        }
    }
}