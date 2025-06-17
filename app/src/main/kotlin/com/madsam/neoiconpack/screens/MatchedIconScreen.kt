package com.madsam.neoiconpack.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.madsam.neoiconpack.model.IconModel
import com.madsam.neoiconpack.util.AllIconsGetter
import com.madsam.neoiconpack.util.ExtraUtil
import com.madsam.neoiconpack.util.InstalledAppReader
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

    // 添加模态框状态
    var selectedIcon by remember { mutableStateOf<IconModel?>(null) }
    var showDialog by remember { mutableStateOf(false) }

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
                            packageName = app.pkg,
                            launcherName = app.launcher
                        )
                    )
                }
            }

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
                        columns = GridCells.Adaptive(minSize = 72.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredIcons) { icon ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    // 更新为显示模态框
                                    selectedIcon = icon
                                    showDialog = true
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
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    lineHeight = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 显示模态框
    if (showDialog && selectedIcon != null) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                securePolicy = SecureFlagPolicy.Inherit
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    // 标题栏：应用名称在左边，关闭按钮在右边
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 图标和名称在左边
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 直接显示图标，无外部Box
                            Image(
                                painter = painterResource(id = selectedIcon!!.iconId),
                                contentDescription = selectedIcon!!.label,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // 应用名称
                            Text(
                                text = selectedIcon!!.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 右上角的关闭按钮
                        IconButton(
                            onClick = { showDialog = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = selectedIcon!!.iconId),
                            contentDescription = selectedIcon!!.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    // 功能按钮
                    Button(
                        onClick = {
                            scope.launch {
                                val iconModel = selectedIcon!!
                                val success = ExtraUtil.sendIcon2HomeScreen(
                                    context,
                                    iconModel.iconId,
                                    iconModel.label,
                                    iconModel.packageName,
                                    iconModel.launcherName
                                )

                                Toast.makeText(
                                    context,
                                    if (success) "图标已添加到主屏幕" else "添加失败",
                                    Toast.LENGTH_SHORT
                                ).show()

                                showDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("添加到主屏幕")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 复制信息按钮
                    Button(
                        onClick = {
                            // 复制组件信息
                            ExtraUtil.copy2Clipboard(
                                context,
                                "应用: ${selectedIcon!!.label}\n包名: ${selectedIcon!!.packageName}\n资源: ${selectedIcon!!.fullResName}"
                            )
                            Toast.makeText(context, "信息已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("复制信息")
                    }
                }
            }
        }
    }
}