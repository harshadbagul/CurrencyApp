package com.andela.currencyapp.data.netowork.service

import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface CurrencyService {

    @GET("symbols")
    suspend fun getAllCurrencies(@HeaderMap headers: Map<String, String>): CurrencyResponse

    @GET("latest")
    suspend fun getLatestRates(
        @HeaderMap headers: Map<String, String>,
        @QueryMap options: Map<String, String>
    ): CurrencyResponse

    @GET("timeseries")
    suspend fun getHistoricData(
        @HeaderMap headers: Map<String, String>,
        @QueryMap options: Map<String, String>
    ): CurrencyResponse

}