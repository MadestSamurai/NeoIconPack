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
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.Log
import java.util.Locale

/**
 * 包工具类，提供应用包信息相关操作
 */
object PkgUtil {
    /**
     * 检查应用是否安装并启用
     */
    fun isPkgInstalledAndEnabled(context: Context?, pkgName: String?): Boolean {
        return getLauncherActivity(context, pkgName) != null
    }

    /**
     * 获取应用启动活动
     */
    fun getLauncherActivity(context: Context?, pkgName: String?): String? {
        if (context == null || pkgName.isNullOrEmpty()) return null

        return try {
            context.packageManager.getLaunchIntentForPackage(pkgName)?.component?.className
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error getting launcher activity for package: $pkgName", e)
            null
        }
    }

    /**
     * 获取当前启动器应用包名
     */
    fun getCurLauncher(context: Context?): String? {
        if (context == null) return null

        return try {
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            context.packageManager.resolveActivity(mainIntent, 0)?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error getting current launcher package", e)
            null
        }
    }

    /**
     * 获取应用的英文标签
     */
    fun getAppLabelEn(context: Context?, pkgName: String?, def: String?): String? {
        if (context == null || pkgName.isNullOrEmpty()) return def

        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getPackageInfo(pkgName, 0).applicationInfo ?: return def

            // 创建英文区域配置
            val englishConfig = Configuration(context.resources.configuration).apply {
                setLocale(Locale.ENGLISH)
            }

            // 创建基于英文配置的上下文
            val englishContext = context.createConfigurationContext(englishConfig)

            // 获取应用资源
            val resources = packageManager.getResourcesForApplication(applicationInfo)

            // 通过英文上下文资源获取应用标签
            val labelResId = applicationInfo.labelRes
            val result = if (labelResId != 0) {
                try {
                    val englishResources = englishContext.createPackageContext(
                        pkgName,
                        Context.CONTEXT_RESTRICTED
                    ).resources
                    englishResources.getString(labelResId)
                } catch (e: Exception) {
                    Log.e("PkgUtil", "获取英文应用名称失败: ${e.message}")
                    resources.getString(labelResId)
                }
            } else {
                applicationInfo.nonLocalizedLabel?.toString() ?: def
            }

            result
        } catch (e: Exception) {
            Log.e("PkgUtil", "获取英文应用名称失败: ${e.message}")
            def
        }
    }

    /**
     * 检查是否为系统应用
     */
    fun isSysApp(context: Context?, pkgName: String?): Boolean {
        if (context == null || pkgName.isNullOrEmpty()) return false

        return try {
            val applicationInfo = context.packageManager.getPackageInfo(pkgName, 0).applicationInfo
            (applicationInfo?.flags?.and((ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) != 0
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error checking if package is system app: $pkgName", e)
            false
        }
    }

    /**
     * 获取应用版本信息
     */
    fun getAppVer(context: Context?, format: String? = "%s"): String {
        if (context == null || format.isNullOrEmpty()) return ""

        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            String.format(Locale.US, format, packageInfo.versionName, packageInfo.longVersionCode)
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error getting app version: ${e.message}")
            ""
        }
    }

    /**
     * 获取应用图标
     */
    fun getIcon(pkgManager: PackageManager?, pkgName: String?): Drawable? {
        if (pkgManager == null || pkgName.isNullOrEmpty()) return null

        return try {
            pkgManager.getPackageInfo(pkgName, 0).applicationInfo?.loadIcon(pkgManager)
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error getting icon for package: $pkgName", e)
            null
        }
    }

    /**
     * 获取Activity图标
     */
    fun getIcon(pkgManager: PackageManager?, pkgName: String?, activity: String?): Drawable? {
        if (pkgManager == null || pkgName.isNullOrEmpty() || activity.isNullOrEmpty()) return null

        return try {
            val intent = Intent().setClassName(pkgName, activity)
            pkgManager.resolveActivity(intent, 0)?.loadIcon(pkgManager)
        } catch (e: Exception) {
            Log.e("PkgUtil", "Error getting icon for activity: $activity in package: $pkgName", e)
            null
        }
    }

    /**
     * 拼接组件名称
     */
    fun concatComponent(pkgName: String, launcherActivity: String?): String {
        if (launcherActivity.isNullOrEmpty()) return pkgName

        return when {
            launcherActivity.startsWith(pkgName) ->
                "$pkgName/${launcherActivity.substring(pkgName.length)}"
            else ->
                "$pkgName/$launcherActivity"
        }
    }
}