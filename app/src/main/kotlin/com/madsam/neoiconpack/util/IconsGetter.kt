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

package com.madsam.neoiconpack.util

import android.content.Context
import com.madsam.neoiconpack.R
import com.madsam.neoiconpack.bean.IconBean
import java.io.Serializable
import java.util.regex.Pattern

/**
 * Created by By_syk on 2017-03-26.
 */
abstract class IconsGetter : Serializable {

    @Throws(Exception::class)
    abstract fun getIcons(context: Context): MutableList<IconBean>

    @Throws(Exception::class)
    protected fun getAllIcons(context: Context): MutableList<IconBean> {
        synchronized(this) {
            if (allIconList != null) {
                return ArrayList(allIconList!!)
            }

            val resources = context.resources
            val names = resources.getStringArray(R.array.icons)
            val labels = resources.getStringArray(R.array.icon_labels)
            val labelPinyins = if (labels.isNotEmpty()) {
                ExtraUtil.getPinyinForSorting(labels)
            } else {
                Array(names.size) { i -> names[i].replace("_", " ") }
            }

            // 优化100以内数值逻辑排序
            for (i in labelPinyins.indices) {
                val matcher = namePattern.matcher(labelPinyins[i])
                if (matcher.find()) {
                    val matchedGroup = matcher.group(0) ?: ""
                    labelPinyins[i] = matcher.replaceAll("0$matchedGroup")
                }
            }

            // 预先加载所有drawable资源ID (编译时确定)
            val drawableClass = R.drawable::class.java
            val drawableFields = drawableClass.fields.associateBy { it.name }

            val dataList = ArrayList<IconBean>()
            for (i in names.indices) {
                // 尝试通过R类直接获取资源ID
                val id = drawableFields[names[i]]?.getInt(null)
                    ?: resources.getIdentifier(names[i], "drawable", context.packageName) // 回退方法

                dataList.add(IconBean(id, names[i], labels.getOrElse(i) { names[i].replace("_", " ") }, labelPinyins[i]))
            }

            // With components
            val appfilterBeanList = AppFilterReader
                .getInstance(resources).dataList

            for (iconBean in dataList) {
                for (filterBean in appfilterBeanList) {
                    if (iconBean.nameNoSeq == filterBean.drawableNoSeq) {
                        iconBean.addComponent(filterBean.pkg, filterBean.launcher)
                        if (iconBean.name == filterBean.drawable) {
                            iconBean.isDef = true
                        }
                    }
                }
            }

            // Check installed
            val installedComponentLabelMap = InstalledAppReader
                .getInstance(context.packageManager).getComponentLabelMap()

            for (bean in dataList) {
                for (component in bean.components) {
                    val componentStr = "${component.pkg}/${component.launcher}"
                    if (installedComponentLabelMap.containsKey(componentStr)) {
                        component.isInstalled = true
                        component.label = installedComponentLabelMap[componentStr]
                    }
                }
            }

            dataList.sort()

            allIconList = ArrayList(dataList)

            return dataList
        }
    }

    companion object {
        private var allIconList: MutableList<IconBean>? = null
        private val namePattern = Pattern.compile("(?<=\\D|^)\\d(?=\\D|$)")
    }
}