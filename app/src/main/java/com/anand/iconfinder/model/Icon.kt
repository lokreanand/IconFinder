package com.anand.iconfinder.model

import com.google.gson.annotations.SerializedName

class Icon (
    @SerializedName("is_premium")
    val isPremium: Boolean,

    val type: String,

    @SerializedName("raster_sizes")
    val rasterSizes: List<RasterSize>,

    val prices: List<Price>,

    @SerializedName("icon_id")
    val id: Int
)