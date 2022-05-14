package com.anand.iconfinder.model

import com.google.gson.annotations.SerializedName

class RasterSize(
    @SerializedName("size_height")
    val sizeHeight: String,

    val size: String,

    @SerializedName("size_width")
    val sizeWidth: String,

    val formats: List<Format>
)