package com.madsam.neoiconpack.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.madsam.neoiconpack.bean.AppBean
import com.madsam.neoiconpack.component.AppItem
import com.madsam.neoiconpack.util.AppFilterReader
import com.madsam.neoiconpack.util.ExtraUtil
import com.madsam.neoiconpack.util.InstalledAppReader
import com.madsam.neoiconpack.util.PkgUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostIconsScreen(
    onIconCountUpdated: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) } // 添加刷新状态
    var searchQuery by remember { mutableStateOf("") }
    val appList = remember { mutableStateListOf<AppBean>() }
    val filteredAppList = remember { mutableStateListOf<AppBean>() }
    val listState = rememberLazyListState()

    // 加载图标的协程作业
    var loadIconsJob by remember { mutableStateOf<Job?>(null) }

    // 初始加载应用数据
    LaunchedEffect(Unit) {
        loadAppList(context, appList)
        filteredAppList.clear()
        filteredAppList.addAll(appList)
        isLoading = false
        onIconCountUpdated(appList.size)
    }

    // 刷新函数
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            loadAppList(context, appList)

            // 重置所有应用的图标状态
            appList.forEach { it.updateIcon(null) }

            filteredAppList.clear()
            filteredAppList.addAll(appList.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.pkg.contains(searchQuery, ignoreCase = true)
            })
            onIconCountUpdated(filteredAppList.size)
            delay(500)
            isRefreshing = false
        }
    }

    // 处理搜索
    LaunchedEffect(searchQuery) {
        filteredAppList.clear()
        filteredAppList.addAll(appList.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.pkg.contains(searchQuery, ignoreCase = true)
        })
        onIconCountUpdated(filteredAppList.size)
    }

    // 修改 LaunchedEffect 的依赖项
    LaunchedEffect(listState, isRefreshing) {
        // 在刷新完成后触发重新加载
        if (!isRefreshing) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.layoutInfo.visibleItemsInfo.size
            }.collectLatest { (firstVisible, visibleCount) ->
                loadIconsJob?.cancel()
                loadIconsJob = scope.launch {
                    if (filteredAppList.isNotEmpty()) {
                        val start = firstVisible
                        val end = (firstVisible + visibleCount + 5).coerceAtMost(filteredAppList.size)

                        for (i in start until end) {
                            if (!isActive) break

                            val app = filteredAppList[i]
                            if (app.icon == null) {
                                val icon = withContext(Dispatchers.IO) {
                                    PkgUtil.getIcon(
                                        context.packageManager,
                                        app.pkg,
                                        app.launcher
                                    )
                                }
                                app.updateIcon(icon)
                            }
                        }
                    }
                }
            }
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            loadIconsJob?.cancel()
        }
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

                // 列表内容
                if (filteredAppList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("没有未适配的图标")
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredAppList) { index, app ->
                            AppItem(
                                app = app,
                                onRequestIcon = {
                                    requestIcon(context, app) { success ->
                                        if (success) {
                                            app.mark = true
                                            app.reqTimes = (app.reqTimes + 1).coerceAtLeast(1)
                                        }
                                    }
                                },
                                onCopyCode = {
                                    copyAppCode(context, app)
                                },
                                onSaveIcon = {
                                    saveAppIcon(context, app)
                                }
                            )
                            if (index < filteredAppList.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 加载应用列表
private suspend fun loadAppList(context: Context, appList: MutableList<AppBean>) {
    withContext(Dispatchers.IO) {
        appList.clear()

        // 使用公共方法获取数据
        val installedAppReader = InstalledAppReader.getInstance(context.packageManager)
        val appFilterReader = AppFilterReader.getInstance(context.resources)

        // 假设已添加这些方法
        val installedApps = installedAppReader.getDataList()
        val appFilterComponents = appFilterReader.componentSet

        android.util.Log.d("LostIconScreen", "安装的应用总数: ${installedApps.size}")
        android.util.Log.d("LostIconScreen", "已适配组件总数: ${appFilterComponents.size}")

        for (app in installedApps) {
            // 跳过已经适配的应用
            if (appFilterComponents.contains("${app.pkg}/${app.launcher}")) {
                continue
            }

            // 添加每个拼音变体作为一个条目
            val pinyinList = ExtraUtil.getPinyinForSorting(app.label)
            for (pinyin in pinyinList) {
                val appBean = AppBean()
                appBean.label = app.label
                appBean.labelPinyin = pinyin
                appBean.pkg = app.pkg
                appBean.launcher = app.launcher
                appBean.reqTimes = 0
                appBean.mark = false

                appList.add(appBean)
            }
        }

        // 排序
        appList.sortWith { app1, app2 ->
            app1.labelPinyin.compareTo(app2.labelPinyin)
        }
    }
}

// 请求图标
private fun requestIcon(context: Context, app: AppBean, onComplete: (Boolean) -> Unit) {
    // 这里应该实现服务器请求逻辑，类似于SubmitReqTask
    // 简化示例，实际中应使用网络请求
    onComplete(true)
}

// 复制应用代码
private fun copyAppCode(context: Context, app: AppBean) {
    val labelEn = PkgUtil.getAppLabelEn(context, app.pkg, null)
    var iconName = ExtraUtil.codeAppName(labelEn)
    if (iconName.isEmpty()) {
        iconName = ExtraUtil.codeAppName(app.label)
    }

    val isSysApp = PkgUtil.isSysApp(context, app.pkg)
    val code = buildString {
        if (isSysApp) {
            append("Build: ${android.os.Build.BRAND}, ${android.os.Build.MODEL}\n")
        }
        append("Label: ${app.label}, ${labelEn}\n")
        append("Component: ${app.pkg}/${app.launcher}, $iconName")
    }

    ExtraUtil.copy2Clipboard(context, code)
}

// 保存应用图标
private fun saveAppIcon(context: Context, app: AppBean) {
    app.icon?.let { icon ->
        ExtraUtil.saveIcon(context, icon, app.label)
    }
}