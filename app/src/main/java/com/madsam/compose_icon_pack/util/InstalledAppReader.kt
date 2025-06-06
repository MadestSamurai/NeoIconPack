package com.madsam.compose_icon_pack.util

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
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val list = packageManager.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in list) {
                dataList.add(
                    Bean(
                        resolveInfo.loadLabel(packageManager).toString(),
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name
                    )
                )
            }
        } catch (e: Exception) {
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
        return dataList.associate { "${it.pkg}/${it.launcher}" to (it.label ?: "") }
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
        val label: String?,
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