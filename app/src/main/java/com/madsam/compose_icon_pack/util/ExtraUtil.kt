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

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
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
        return arrayOf(text!!.toLowerCase())
    }

    /**
     * 简单替代原拼音转换功能，仅保留输入文本
     */
    fun getPinyinForSorting(textArr: Array<String>?): Array<String> {
        if (textArr == null) {
            return emptyArray()
        }

        return Array(textArr.size) { i ->
            textArr[i].toLowerCase()
        }
    }

    /**
     * 复制文本到剪切板
     *
     * @param context
     * @param text
     */
    @TargetApi(11)
    fun copy2Clipboard(context: Context?, text: String?) {
        if (context == null || text == null) {
            return
        }

        if (C.SDK >= 11) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(null, text)
            clipboardManager.setPrimaryClip(clipData)
        } else {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboardManager.text = text
        }
    }

    fun codeAppName(name: String?): String {
        if (name == null) {
            return ""
        }
        var processedName = name.trim()
        if (processedName.isEmpty()) {
            return ""
        }
        // Not "[A-Za-z][A-Za-z\\d'\\+-\\. _]*"
        if (!processedName.matches(Regex("[A-Za-z][A-Za-z\\d'\\+\\-\\. _]*"))) {
            return ""
        }
        val matcher = codePattern.matcher(processedName)
        while (matcher.find()) {
            processedName = processedName.replace(matcher.group(0),
                "${matcher.group(0)[0]}_${matcher.group(0)[1]}")
        }
        return processedName.toLowerCase()
            .replace("'", "")
            .replace("\\+".toRegex(), "_plus")
            .replace("[-. ]".toRegex(), "_")
            .replace("_{2,}".toRegex(), "_")
    }

    fun saveIcon(context: Context?, drawable: Drawable?, name: String?): Boolean {
        if (context == null || drawable == null || TextUtils.isEmpty(name)) {
            return false
        }

        val bitmap = (drawable as BitmapDrawable).bitmap ?: return false

        // Create a path where we will place our picture
        // in the user's public pictures directory.
        val picDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Icons")
        // Make sure the Pictures directory exists.
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

        record2Gallery(context, targetFile, false)
        return true
    }

    /**
     * 记录新增图片文件到媒体库，这样可迅速在系统图库看到
     *
     * @param context
     * @param newlyPicFile
     * @return
     */
    private fun record2Gallery(context: Context, newlyPicFile: File?, allInDir: Boolean): Boolean {
        if (context == null || newlyPicFile == null || !newlyPicFile.exists()) {
            return false
        }

        Log.d(C.LOG_TAG, "record2Gallery(): $newlyPicFile, $allInDir")

        if (C.SDK >= 19) {
            val filePaths = if (allInDir) {
                newlyPicFile.parentFile?.list() ?: return false
            } else {
                arrayOf(newlyPicFile.path)
            }
            MediaScannerConnection.scanFile(context, filePaths, null, null)
        } else {
            if (allInDir) {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.fromFile(newlyPicFile.parentFile)))
            } else {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(newlyPicFile)))
            }
        }

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

        val networkInfo = connectivityManager.activeNetworkInfo ?: return false

        var isConnected = networkInfo.isAvailable
        if (isWifiOnly) {
            isConnected = isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }

        return isConnected
    }

    fun isNetworkConnected(context: Context?): Boolean {
        return isNetworkConnected(context, false)
    }

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        if (!TextUtils.isEmpty(androidId)) {
            val serial = Build.SERIAL
            if ("unknown".equals(serial, ignoreCase = true).not()) {
                return androidId + serial
            }
            return androidId
        }

        val file = File(context.filesDir, "deviceId")
        file.mkdir()
        val files = file.listFiles()
        if (files?.isNotEmpty() == true) {
            return files[0].name
        }
        val id = UUID.randomUUID().toString()
        File(file, id).mkdir()
        return id
    }

    fun gotoMarket(context: Context?, pkgName: String?, viaBrowser: Boolean) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return
        }

        // https://play.google.com/store/apps/details?id=%s
        val LINK = String.format(
            if (viaBrowser) "http://www.coolapk.com/apk/%s" else "market://details?id=%s",
            pkgName
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(LINK)

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

        val ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"

        val addIntent = Intent()
        addIntent.action = ACTION_ADD_SHORTCUT
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
        addIntent.putExtra(
            Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(context, iconId)
        )
        addIntent.putExtra("duplicate", false)
        context.sendBroadcast(addIntent)

        return true
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