package com.example.smartexpensemanager.data.repository

import com.example.smartexpensemanager.data.remote.RetrofitClient
import com.example.smartexpensemanager.util.Result
import java.io.IOException

class ExchangeRateRepository {

    suspend fun convert(amount: Double, targetCurrency: String, baseCurrency: String = "KES"): Result<Double> {
        return try {
            val response = RetrofitClient.ratesApi.getLatestRates(baseCurrency)
            if (response.result != "success") {
                return Result.Error("network_error")
            }
            val rate = response.rates[targetCurrency]
                ?: return Result.Error("unknown_currency")
            Result.Success(amount * rate)
        } catch (e: IOException) {
            Result.Error("network_error")
        } catch (e: Exception) {
            Result.Error(e.message ?: "network_error")
        }
    }
}
