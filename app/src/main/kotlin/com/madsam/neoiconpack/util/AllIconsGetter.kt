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
import com.madsam.neoiconpack.bean.IconBean
import java.io.Serializable

/**
 * Created by By_syk on 2017-03-26.
 */
class AllIconsGetter : IconsGetter(), Serializable {
    @Throws(Exception::class)
    override fun getIcons(context: Context): MutableList<IconBean> {
        return getAllIcons(context)
    }
}
