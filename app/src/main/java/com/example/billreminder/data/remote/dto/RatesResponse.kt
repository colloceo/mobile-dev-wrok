package com.example.billreminder.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RatesResponse(
    val result: String,
    @SerializedName("base_code") val baseCode: String,
    val rates: Map<String, Double>
)
