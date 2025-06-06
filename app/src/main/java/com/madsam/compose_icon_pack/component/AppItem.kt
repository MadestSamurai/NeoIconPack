package com.madsam.compose_icon_pack.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.madsam.compose_icon_pack.bean.AppBean

@Composable
fun AppItem(
    app: AppBean,
    onRequestIcon: () -> Unit,
    onCopyCode: () -> Unit,
    onSaveIcon: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = app.icon
                if (icon != null) {
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 应用信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = app.pkg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (app.reqTimes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (app.mark) Color.Green else Color.Red,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "请求次数: ${app.reqTimes}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                // 复制代码按钮
                IconButton(onClick = onCopyCode) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制代码",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // 请求图标按钮
                IconButton(onClick = onRequestIcon) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "请求图标",
                        tint = if (app.mark) Color.Green else MaterialTheme.colorScheme.primary
                    )
                }

                // 保存图标按钮
                IconButton(onClick = onSaveIcon) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "保存图标",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}