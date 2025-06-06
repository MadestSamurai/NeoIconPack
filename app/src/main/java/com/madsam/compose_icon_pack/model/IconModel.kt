package com.madsam.compose_icon_pack.model

data class IconModel(
    val iconId: Int,
    val label: String,
    val fullResName: String = "",
    val packageName: String = ""
)