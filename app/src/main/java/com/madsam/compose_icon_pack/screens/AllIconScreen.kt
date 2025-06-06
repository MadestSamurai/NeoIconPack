package com.madsam.compose_icon_pack.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.madsam.compose_icon_pack.model.IconModel
import com.madsam.compose_icon_pack.util.AllIconsGetter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllIconsScreen(
    onIconCountUpdated: (Int) -> Unit,
    onShowAbout: () -> Unit,
    onShowSearch: () -> Unit,
    onShowWhatsNew: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var icons by remember { mutableStateOf<List<IconModel>>(emptyList()) }

    LaunchedEffect(true) {
        // 假设AllIconsGetter已转换为Kotlin并支持协程
        AllIconsGetter().getIcons(context)?.let {
            icons = it
            onIconCountUpdated(it.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全部图标") },
                actions = {
                    IconButton(onClick = onShowSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onShowWhatsNew) {
                        Icon(Icons.Default.Whatshot, contentDescription = "新增图标")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Info, contentDescription = "关于")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("关于") },
                            onClick = {
                                showMenu = false
                                onShowAbout()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (icons.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(icons) { icon ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // 处理图标点击
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = icon.iconId),
                                contentDescription = icon.label,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = icon.label,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("正在加载图标...")
            }
        }
    }
}