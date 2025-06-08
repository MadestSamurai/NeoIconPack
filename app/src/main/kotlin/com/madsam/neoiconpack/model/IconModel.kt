package com.madsam.neoiconpack.model

data class IconModel(
    val iconId: Int,
    val label: String,
    val fullResName: String = "",
    val packageName: String = "",
    val launcherName: String = ""
)