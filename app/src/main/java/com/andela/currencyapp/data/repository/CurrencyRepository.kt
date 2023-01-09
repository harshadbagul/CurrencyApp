package com.andela.currencyapp.data.repository

import com.andela.currencyapp.BuildConfig
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.service.CurrencyService
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.utils.Constants.API_KEY_NAME
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_BASE
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_SYMBOLS
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyService: CurrencyService,
    private val currencyDatabase: CurrencyDatabase
) {

    suspend fun getAllCurrencies(): kotlinx.coroutines.flow.Flow<CurrencyState> {
        return flow {
            val response = getCurrencies()
            when(response.success){
                true -> {
                    emit(CurrencyState.Success(response))
                    val allCurrencies = convertAllCurrenciesToList(response.symbols)
                    currencyDatabase.currencyDao.insertAll(allCurrencies)
                }
                false -> { emit(CurrencyState.Error(response.errorResponse)) }
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun getCurrencies(): CurrencyResponse {
        return withContext(Dispatchers.IO) {
            currencyService.getAllCurrencies(mapOf(API_KEY_NAME to BuildConfig.API_KEY))
        }
    }

    suspend fun getLatestRates(base:String): kotlinx.coroutines.flow.Flow<CurrencyState> {
        return flow {
            // fetch all symbols from db
            val symbols = currencyDatabase.currencyDao.getAllDbCurrencies().map { it.symbol }

            // get latest rate rates for symbols
            val response = latestRates(base,symbols)

            response?.let { currencyResponse ->
                when(currencyResponse.success){
                    true -> {
                        emit(CurrencyState.Success(currencyResponse))
                        val rates = currencyResponse.rates as? Map<*, *>
                        currencyDatabase.currencyDao.updateDbCurrencies(convertAllRateToList(rates))
                    }
                    false -> { emit(CurrencyState.Error(currencyResponse.errorResponse)) }
                }
            }

        }.flowOn(Dispatchers.IO)
    }


    private suspend fun latestRates(base:String, symbols:List<String>): CurrencyResponse? {
        val query = mapOf(
            API_KEY_NAME to BuildConfig.API_KEY,
            QUERY_PARAM_BASE to base,
            QUERY_PARAM_SYMBOLS to symbols.joinToString()
        )
        return try {
            withContext(Dispatchers.IO) {
                currencyService.getLatestRates(
                    mapOf(API_KEY_NAME to BuildConfig.API_KEY),
                    query)
            }
        } catch (e: java.lang.Exception){
            e.printStackTrace()
            return null
        }
    }

    private fun convertAllCurrenciesToList(symbols: Map<String, String>?): List<Currency> {
        val symbolList = ArrayList<Currency>()
        symbols?.map {
            symbolList.add(Currency(symbol = it.key, name = it.value))
        }
        return symbolList
    }

    private fun convertAllRateToList(rates: Map<*, *>?): List<Currency> {
        val symbolList = ArrayList<Currency>()
        rates?.map {
            symbolList.add(Currency(symbol = it.key as String, rate = it.value as Double))
        }
        return symbolList
    }

}