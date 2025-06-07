package com.madsam.compose_icon_pack.screens

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.madsam.compose_icon_pack.model.IconModel
import com.madsam.compose_icon_pack.util.AllIconsGetter
import com.madsam.compose_icon_pack.util.ExtraUtil
import com.madsam.compose_icon_pack.util.InstalledAppReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchedIconsScreen(
    onIconCountUpdated: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var icons by remember { mutableStateOf<List<IconModel>>(emptyList()) }
    var filteredIcons by remember { mutableStateOf<List<IconModel>>(emptyList()) }

    // 加载已安装且已适配图标的函数
    suspend fun loadMatchedIcons(): List<IconModel> {
        return withContext(Dispatchers.IO) {
            val installedAppReader = InstalledAppReader.getInstance(context.packageManager)

            val installedApps = installedAppReader.getDataList()
            val matchedIcons = mutableListOf<IconModel>()
            val iconBeans = AllIconsGetter().getIcons(context)

            val iconsByPackage = iconBeans.groupBy { it.components.firstOrNull()?.pkg ?: "" }

            for (app in installedApps) {
                val matchingIcons = iconsByPackage[app.pkg]
                val iconBean = matchingIcons?.firstOrNull()

                if (iconBean != null) {
                    matchedIcons.add(
                        IconModel(
                            iconId = iconBean.iconId,
                            label = app.label,
                            fullResName = iconBean.name,
                            packageName = app.pkg
                        )
                    )
                }
            }

            // 按拼音排序
            matchedIcons.sortedBy { icon ->
                ExtraUtil.getPinyinForSorting(icon.label).firstOrNull() ?: ""
            }
        }
    }

    // 初始加载图标数据
    LaunchedEffect(Unit) {
        icons = loadMatchedIcons()
        filteredIcons = icons
        isLoading = false
        onIconCountUpdated(icons.size)
    }

    // 刷新函数
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            icons = loadMatchedIcons()

            // 应用搜索过滤
            filteredIcons = icons.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }

            onIconCountUpdated(filteredIcons.size)
            delay(500)
            isRefreshing = false
        }
    }

    // 处理搜索
    LaunchedEffect(searchQuery) {
        filteredIcons = icons.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
        onIconCountUpdated(filteredIcons.size)
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("搜索应用") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                    singleLine = true
                )

                // 网格内容
                if (filteredIcons.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("没有找到适配的图标")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 80.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredIcons) { icon ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    // 点击复制组件信息
                                    ExtraUtil.copy2Clipboard(
                                        context,
                                        "应用: ${icon.label}\n包名: ${icon.packageName}\n资源: ${icon.fullResName}"
                                    )
                                }
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
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = icon.label,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}