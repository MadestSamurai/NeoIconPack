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

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

/**
 * Created by By_syk on 2017-01-23.
 */
object ExtraUtil {
    private val codePattern = Pattern.compile("([a-z][A-Z])|([A-Za-z]\\d)|(\\d[A-Za-z])")

    /**
     * 简单替代原拼音转换功能，仅保留输入文本
     *
     * @param text
     * @return
     */
    fun getPinyinForSorting(text: String?): Array<String> {
        if (TextUtils.isEmpty(text)) {
            return arrayOf("")
        }
        return arrayOf(text!!.lowercase(Locale.ROOT))
    }

    /**
     * 简单替代原拼音转换功能，仅保留输入文本
     */
    fun getPinyinForSorting(textArr: Array<String>?): Array<String> {
        if (textArr == null) {
            return emptyArray()
        }

        return Array(textArr.size) { i ->
            textArr[i].lowercase(Locale.ROOT)
        }
    }

    /**
     * 获取应用版本号（versionCode）
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(C.LOG_TAG, "getAppVersionCode() failed: ${e.message}")
            0L
        }
    }

    /**
     * 复制文本到剪切板
     *
     * @param context
     * @param text
     */
    fun copy2Clipboard(context: Context?, text: String?) {
        if (context == null || text == null) {
            return
        }
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun codeAppName(name: String?): String {
        if (name == null) {
            return ""
        }
        var processedName = name.trim()
        if (processedName.isEmpty()) {
            return ""
        }

        if (!processedName.matches(Regex("[A-Za-z][A-Za-z\\d'+\\-. _]*"))) {
            return ""
        }

        val matcher = codePattern.matcher(processedName)
        val sb = StringBuilder(processedName)
        var offset = 0

        while (matcher.find()) {
            val matchStr = matcher.group(0)
            val matchPos = matcher.start()
            if (matchStr != null && matchStr.length >= 2) {
                sb.replace(matchPos + offset, matchPos + matchStr.length + offset,
                    "${matchStr[0]}_${matchStr[1]}")
                offset += 1
            }
        }

        return sb.toString()
            .lowercase(Locale.ROOT)
            .replace("'", "")
            .replace("\\+".toRegex(), "_plus")
            .replace("[-. ]".toRegex(), "_")
            .replace("_{2,}".toRegex(), "_")
    }

    fun saveIcon(context: Context?, drawable: Drawable?, name: String?): Boolean {
        if (context == null || drawable == null || TextUtils.isEmpty(name)) {
            return false
        }

        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                try {
                    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
                    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192

                    val bitmap = createBitmap(width, height)
                    val canvas = android.graphics.Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            }
        }

        val picDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Icons")
        picDir.mkdirs()
        val targetFile = File(picDir, "ic_${name}_${bitmap.byteCount}.png")

        var result = false
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(targetFile)
            result = bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (!result) {
            targetFile.delete()
            return false
        }

        record2Gallery(context, targetFile)
        return true
    }

    /**
     * 记录新增图片文件到媒体库，这样可迅速在系统图库看到
     *
     * @param context 上下文
     * @param picFile 图片文件
     * @return 是否成功通知媒体库
     */
    private fun record2Gallery(context: Context, picFile: File?): Boolean {
        if (picFile == null || !picFile.exists()) {
            return false
        }

        Log.d(C.LOG_TAG, "record2Gallery(): $picFile")

        // 始终使用 MediaScannerConnection，更可靠且兼容性更好
        MediaScannerConnection.scanFile(
            context,
            arrayOf(picFile.absolutePath),
            arrayOf("image/png"),  // 指定 MIME 类型提高扫描精确度
            null
        )

        return true
    }

    fun shareText(context: Context?, content: String?, hint: String?) {
        if (context == null || TextUtils.isEmpty(content)) {
            return
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, content)
        try {
            if (TextUtils.isEmpty(hint)) {
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, hint))
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun isNetworkConnected(context: Context?, isWifiOnly: Boolean): Boolean {
        if (context == null) {
            return false
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        if (!hasInternet) return false

        if (isWifiOnly) {
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }

        return true
    }

    fun isNetworkConnected(context: Context?): Boolean {
        return isNetworkConnected(context, false)
    }

    /**
     * 获取设备标识符，遵循现代 Android 隐私实践
     *
     * @param context 应用上下文
     * @return 设备标识符字符串
     */
    fun getDeviceId(context: Context): String {
        // 首选方案：使用安装ID - 这个ID在应用卸载重装后会改变
        val installationID = getOrCreateInstallationId(context)

        // 结合使用其他信息来增强唯一性
        val deviceInfo = Build.MANUFACTURER + Build.MODEL

        return "$installationID:$deviceInfo"
    }

    /**
     * 获取或创建应用安装ID
     */
    private fun getOrCreateInstallationId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("app_installation", Context.MODE_PRIVATE)
        var installationId = sharedPrefs.getString("installation_id", null)

        if (installationId == null) {
            installationId = UUID.randomUUID().toString()
            sharedPrefs.edit { putString("installation_id", installationId) }
        }

        return installationId
    }

    fun gotoMarket(context: Context?, pkgName: String?, viaBrowser: Boolean) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return
        }

        // https://play.google.com/store/apps/details?id=%s
        val link = String.format(
            if (viaBrowser) "http://www.coolapk.com/apk/%s" else "market://details?id=%s",
            pkgName
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = link.toUri()

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()

            if (!viaBrowser) {
                gotoMarket(context, pkgName, true)
            }
        }
    }

    fun renderReqTimes(reqTimes: Int): String {
        val safeReqTimes = if (reqTimes < 0) 0 else reqTimes
        return C.REQ_REDRAW_PREFIX + safeReqTimes
    }

    fun fetchColor(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(attrId)
        )
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun sendIcon2HomeScreen(
        context: Context?, iconId: Int, appName: String?,
        pkgName: String?, launcherName: String?
    ): Boolean {
        if (context == null || iconId == 0 || TextUtils.isEmpty(appName)
            || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(launcherName)) {
            return false
        }

        val shortcutIntent = Intent(Intent.ACTION_VIEW)
        shortcutIntent.setClassName(pkgName!!, launcherName!!)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // 使用 ShortcutManager API
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
            val shortcutId = "$pkgName:$launcherName"
            val icon = Icon.createWithResource(context, iconId)

            val pinShortcutInfo = ShortcutInfo.Builder(context, shortcutId)
                .setIntent(shortcutIntent)
                .setShortLabel(appName!!)
                .setIcon(icon)
                .build()

            val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(
                pinShortcutInfo
            )

            val successCallback = PendingIntent.getBroadcast(
                context, 0,
                pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
            )

            return shortcutManager.requestPinShortcut(
                pinShortcutInfo,
                successCallback.intentSender
            )
        }
        return false
    }

    fun purifyIconName(iconName: String?): String {
        if (TextUtils.isEmpty(iconName)) {
            return ""
        }
        if (iconName!!.matches(Regex(".+?_\\d+"))) {
            return iconName.substring(0, iconName.lastIndexOf('_'))
        }
        return iconName
    }
}