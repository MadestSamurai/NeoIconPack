package com.madsam.neoiconpack.util

import android.content.Intent
import android.content.pm.PackageManager

/**
 * Created by By_syk on 2017-07-06.
 */
class InstalledAppReader private constructor(packageManager: PackageManager) {
    private val dataList: MutableList<Bean> = ArrayList()

    init {
        init(packageManager)
    }

    private fun init(packageManager: PackageManager) {
        try {
            // 只获取有启动器图标的应用
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val launcherApps = packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.GET_META_DATA or PackageManager.MATCH_ALL
            )

            android.util.Log.d("InstalledAppReader", "带启动器应用数量: ${launcherApps.size}")

            // 只添加启动器应用
            for (resolveInfo in launcherApps) {
                dataList.add(
                    Bean(
                        resolveInfo.loadLabel(packageManager).toString(),
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name
                    )
                )
            }

            android.util.Log.d("InstalledAppReader", "收集的应用总数: ${dataList.size}")
        } catch (e: Exception) {
            android.util.Log.e("InstalledAppReader", "初始化失败", e)
            e.printStackTrace()
        }
    }

    fun getDataList(): List<Bean> {
        return dataList
    }

    fun getComponentSet(): Set<String> {
        return dataList.map { "${it.pkg}/${it.launcher}" }.toSet()
    }

    fun getComponentLabelMap(): Map<String, String> {
        return dataList.associate { "${it.pkg}/${it.launcher}" to it.label }
    }

    fun getLabel(component: String): String? {
        for (bean in dataList) {
            if (component == "${bean.pkg}/${bean.launcher}") {
                return bean.label
            }
        }
        return null
    }

    inner class Bean(
        val label: String,
        val pkg: String,
        val launcher: String
    )

    companion object {
        private var instance: InstalledAppReader? = null

        @JvmStatic
        fun getInstance(packageManager: PackageManager): InstalledAppReader {
            if (instance == null) {
                synchronized(InstalledAppReader::class.java) {
                    if (instance == null) {
                        instance = InstalledAppReader(packageManager)
                    }
                }
            }
            return instance!!
        }
    }
}