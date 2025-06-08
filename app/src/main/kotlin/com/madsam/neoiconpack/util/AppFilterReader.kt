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

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.text.TextUtils
import com.madsam.neoiconpack.R
import org.xmlpull.v1.XmlPullParser
import java.util.regex.Pattern

/**
 * Created by By_syk on 2017-03-04.
 */
class AppFilterReader private constructor(resources: Resources) {
    val dataList: MutableList<Bean> = ArrayList()

    init {
        init(resources)
    }

    private fun init(resources: Resources): Boolean {
        try {
            val parser: XmlResourceParser = resources.getXml(R.xml.appfilter)
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (parser.name != "item") {
                        event = parser.next()
                        continue
                    }
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (TextUtils.isEmpty(drawable)) {
                        event = parser.next()
                        continue
                    }
                    val component = parser.getAttributeValue(null, "component")
                    if (TextUtils.isEmpty(component)) {
                        event = parser.next()
                        continue
                    }
                    val matcher = componentPattern.matcher(component)
                    if (!matcher.matches()) {
                        event = parser.next()
                        continue
                    }
                    val pkg = matcher.group(1) ?: continue
                    val launcher = matcher.group(2) ?: continue
                    dataList.add(Bean(pkg, launcher, drawable))
                }
                event = parser.next()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    val pkgSet: Set<String>
        get() = dataList.map { it.pkg }.toSet()

    val componentSet: Set<String>
        get() = dataList.map { "${it.pkg}/${it.launcher}" }.toSet()

    inner class Bean(
        val pkg: String,
        val launcher: String,
        val drawable: String
    ) {
        // extra
        val drawableNoSeq: String = ExtraUtil.purifyIconName(drawable)
    }

    companion object {
        private var instance: AppFilterReader? = null
        private val componentPattern = Pattern.compile("ComponentInfo\\{([^/]+?)/(.+?)\\}")

        @JvmStatic
        fun getInstance(resources: Resources): AppFilterReader {
            if (instance == null) {
                synchronized(AppFilterReader::class.java) {
                    if (instance == null) {
                        instance = AppFilterReader(resources)
                    }
                }
            }
            return instance!!
        }
    }
}