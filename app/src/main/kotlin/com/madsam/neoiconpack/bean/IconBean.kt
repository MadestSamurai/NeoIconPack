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

package com.madsam.neoiconpack.bean

import com.madsam.neoiconpack.util.ExtraUtil
import java.io.Serializable

/**
 * 图标数据类
 */
data class IconBean(
    var iconId: Int,
    var name: String
) : Serializable, Comparable<IconBean> {

    // 提取后的名称（无序列号）
    val nameNoSeq: String = ExtraUtil.purifyIconName(name)

    // 图标标签
    var label: String? = null

    // 拼音（用于排序）
    var labelPinyin: String? = null

    // 组件集合
    val components: MutableSet<Component> = HashSet()

    // 是否为默认图标
    var isDef: Boolean = false

    // 是否已被记录
    val isRecorded: Boolean
        get() = components.isNotEmpty()

    /**
     * 添加组件
     */
    fun addComponent(pkg: String?, launcher: String?): Boolean {
        if (pkg == null || launcher == null) return false

        return components.add(Component(pkg, launcher))
    }

    /**
     * 检查是否包含已安装的组件
     */
    fun containsInstalledComponent(): Boolean {
        return components.any { it.isInstalled }
    }

    /**
     * 二次构造函数，包含标签和拼音
     */
    constructor(id: Int, name: String, label: String?, labelPinyin: String?) : this(id, name) {
        this.label = label
        this.labelPinyin = labelPinyin
    }

    override fun compareTo(other: IconBean): Int {
        // 安全处理拼音可能为null的情况
        val thisPinyin = this.labelPinyin ?: ""
        val otherPinyin = other.labelPinyin ?: ""
        return thisPinyin.compareTo(otherPinyin)
    }

    /**
     * 组件数据类
     */
    data class Component(
        val pkg: String,
        val launcher: String
    ) : Serializable {

        // 组件标签
        var label: String? = null

        // 是否已安装
        var isInstalled: Boolean = false
    }
}