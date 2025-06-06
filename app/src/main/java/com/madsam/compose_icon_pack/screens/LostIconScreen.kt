package com.madsam.compose_icon_pack.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LostIconsScreen(
    onIconCountUpdated: (Int) -> Unit,
    onDoubleClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "未适配图标")
    }

    // 假设有20个未适配图标
    onIconCountUpdated(20)
}