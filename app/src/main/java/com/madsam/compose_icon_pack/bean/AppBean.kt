package com.madsam.compose_icon_pack.bean

import android.graphics.drawable.Drawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AppBean(
    var label: String = "",
    var labelPinyin: String = "",
    var pkg: String = "",
    var launcher: String = "",
    var reqTimes: Int = -1,
    var mark: Boolean = false,
    var _icon: Drawable? = null
) : Comparable<AppBean> {
    // 使用可变状态存储图标
    var icon by mutableStateOf<Drawable?>(_icon)
        private set

    // 更新图标的函数
    fun updateIcon(drawable: Drawable?) {
        icon = drawable
    }

    override fun compareTo(other: AppBean): Int {
        return labelPinyin.compareTo(other.labelPinyin)
    }
}