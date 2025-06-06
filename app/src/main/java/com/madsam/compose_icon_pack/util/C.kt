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
package com.madsam.compose_icon_pack.util

import android.os.Build

/**
 * Created by By_syk on 2016-07-16.
 */
object C {
    val SDK: Int = Build.VERSION.SDK_INT

    const val LOG_TAG: String = "NANO_ICON_PACK"

    const val ICONS_COUNT = 100 // 替换为您图标包的实际图标数量

    const val APP_CODE_COMPONENT: String =
        "<item component=\"ComponentInfo{%1\$s/%2\$s}\" drawable=\"%3\$s\" />"
    const val APP_CODE_LABEL: String = "<!-- %1\$s / %2\$s -->"
    const val APP_CODE_BUILD: String = "<!-- Build: %1\$s / %2\$s -->"

    const val URL_NANO_SERVER: String = "http://by-syk.com:8081/nanoiconpack/"

    //    public static final String URL_NANO_SERVER = "http://192.168.43.76:8082/nanoiconpack/";
    const val URL_COOLAPK_API: String = "https://api.coolapk.com/v6/"

    //    public static final String REQ_REDRAW_PREFIX = "\uD83C\uDE38 ";
    //    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDE4F ";
    //    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDCCE ";
    //    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDC65 ";
    const val REQ_REDRAW_PREFIX: String = "\uD83D\uDC64 "

    //    public static final String ICON_ONE_SUFFIX = " ◎";
    const val ICON_ONE_SUFFIX: String = " ·"
}
