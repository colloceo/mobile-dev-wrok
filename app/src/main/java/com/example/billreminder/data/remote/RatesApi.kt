package com.example.billreminder.data.remote

import com.example.billreminder.data.remote.dto.RatesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RatesApi {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String): RatesResponse
}
