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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.madsam.compose_icon_pack.bean.IconBean
import com.madsam.compose_icon_pack.dialog.IconDetailsDialog
import com.madsam.compose_icon_pack.ui.theme.ComposeIconPackTheme
import com.madsam.compose_icon_pack.util.AllIconsGetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeIconPackTheme {
                SearchScreen(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var allIcons by remember { mutableStateOf<List<IconBean>>(emptyList()) }
    var filteredIcons by remember { mutableStateOf<List<IconBean>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var showIconDetails by remember { mutableStateOf<IconBean?>(null) }

    // 加载所有图标
    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            AllIconsGetter().getIcons(context).let { icons ->
                allIcons = icons
                isLoading = false
            }
        }
    }

    // 根据搜索词过滤图标
    LaunchedEffect(query, allIcons) {
        if (query.isEmpty()) {
            filteredIcons = emptyList()
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            val keyword = query.lowercase()
            filteredIcons = allIcons.filter { icon ->
                // 检查组件匹配
                val componentMatch = icon.components.any { component ->
                    component.pkg.contains(keyword) ||
                            (component.label?.lowercase()?.contains(keyword) == true)
                }

                // 检查标签或名称匹配
                val labelMatch = icon.label?.lowercase()?.contains(keyword) == true
                val nameMatch = icon.name.lowercase().contains(keyword)

                componentMatch || labelMatch || nameMatch
            }.sortedBy { it.labelPinyin ?: it.label ?: it.name }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 搜索栏
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = { Text(stringResource(id = R.string.search_hint)) },
                    leadingIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 可以在这里添加搜索建议
                }

                // 搜索结果或加载状态
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (query.isEmpty()) {
                    // 等待用户输入搜索词
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "请输入搜索关键词",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (filteredIcons.isEmpty()) {
                    // 没有搜索结果
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有找到匹配的图标",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // 显示搜索结果
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 80.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredIcons) { icon ->
                            IconItem(
                                icon = icon,
                                onClick = { showIconDetails = icon }
                            )
                        }
                    }
                }
            }

            // 显示图标详情对话框
            showIconDetails?.let { icon ->
                IconDetailsDialog(
                    icon = icon,
                    onDismiss = { showIconDetails = null }
                )
            }
        }
    }
}

@Composable
fun IconItem(
    icon: IconBean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon.iconId),
                contentDescription = icon.label,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }

        Text(
            text = icon.label ?: icon.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}