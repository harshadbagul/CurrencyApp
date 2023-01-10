package com.andela.currencyapp.data.repository

import com.andela.currencyapp.BuildConfig
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.service.CurrencyService
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.utils.Constants.API_KEY_NAME
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_BASE
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_END_DATE
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_START_DATE
import com.andela.currencyapp.data.utils.Constants.QUERY_PARAM_SYMBOLS
import com.andela.currencyapp.data.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyService: CurrencyService,
    private val currencyDatabase: CurrencyDatabase
) {

    /**
     *   Flow to produce all symbols from coroutines
     *   @param
     *   @return Flow<CurrencyState>
     */
    suspend fun getAllCurrencies(): kotlinx.coroutines.flow.Flow<CurrencyState> {
        return flow {
            val response = getCurrencies()
            response?.let { resp ->
                when (resp.success) {
                    true -> {
                        emit(CurrencyState.Success(resp))
                        val allCurrencies = convertAllCurrenciesToList(resp.symbols)
                        currencyDatabase.currencyDao.insertAll(allCurrencies)
                    }
                    false -> {
                        emit(CurrencyState.Error(resp.errorResponse))
                    }
                }
            } ?: run {
                emit(CurrencyState.Error(null))
            }

        }.flowOn(Dispatchers.IO)
    }

    /**
     * Coroutine to fetch all symbols from api
     */
    private suspend fun getCurrencies(): CurrencyResponse? {
        return try {
            withContext(Dispatchers.IO) {
                currencyService.getAllCurrencies(mapOf(API_KEY_NAME to BuildConfig.API_KEY))
            }
        }catch (e: Exception){
            e.printStackTrace()
            return null
        }
    }


    /**
     *   Flow to produce latest rates from coroutines
     *   @param base - from spinner selection consider as base
     *   @return Flow<CurrencyState>
     */
    suspend fun getLatestRates(base: String): kotlinx.coroutines.flow.Flow<CurrencyState> {
        return flow {
            // fetch all symbols from db
            val symbols = currencyDatabase.currencyDao.getAllDbCurrencies().map { it.symbol }

            // get latest rate rates for symbols
            val response = latestRates(base, symbols)

            response?.let {
                when (response.success) {
                    true -> {
                        emit(CurrencyState.Success(response))
                        val rates = response.rates as? Map<*, *>
                        currencyDatabase.currencyDao.updateDbCurrencies(convertAllRateToList(rates))
                    }
                    false -> {
                        emit(CurrencyState.Error(response.errorResponse))
                    }
                }
            } ?: run {
                emit(CurrencyState.Error(null))
            }

        }.flowOn(Dispatchers.IO)
    }


    /**
     *   Flow to produce historic rates from coroutines
     *   @param base - from spinner selection consider as base
     *   @param symbols - input get historic data for symbols
     *   @return Flow<CurrencyState>
     */
    suspend fun getHistoricData(
        base: String,
        symbols: List<String>
    ): kotlinx.coroutines.flow.Flow<CurrencyState> {
        return flow {

            val historicDataResponse = historicRates(base, symbols)
            historicDataResponse?.let {
                when (historicDataResponse.success) {
                    true -> {
                        emit(CurrencyState.Success(historicDataResponse))
                    }

                    false -> {
                        emit(CurrencyState.Error(historicDataResponse.errorResponse))
                    }
                }
            } ?: run {
                emit(CurrencyState.Error(null))
            }

        }.flowOn(Dispatchers.IO)
    }

    /**
     * Coroutine to fetch latest rates for symbols from api
     */
    private suspend fun latestRates(base: String, symbols: List<String>): CurrencyResponse? {
        val query = mapOf(
            API_KEY_NAME to BuildConfig.API_KEY,
            QUERY_PARAM_BASE to base,
            QUERY_PARAM_SYMBOLS to symbols.joinToString()
        )
        return try {
            withContext(Dispatchers.IO) {
                currencyService.getLatestRates(
                    mapOf(API_KEY_NAME to BuildConfig.API_KEY),
                    query
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Coroutine to fetch historic rates for symbols from api
     */
    private suspend fun historicRates(base: String, symbols: List<String>): CurrencyResponse? {
        val query = mapOf(
            API_KEY_NAME to BuildConfig.API_KEY,
            QUERY_PARAM_START_DATE to DateUtils.getDateBeforeDays(-3),
            QUERY_PARAM_END_DATE to DateUtils.getDateBeforeDays(-1),
            QUERY_PARAM_BASE to base,
            QUERY_PARAM_SYMBOLS to symbols.joinToString()
        )

        return try {
            withContext(Dispatchers.IO) {
                currencyService.getHistoricData(
                    mapOf(API_KEY_NAME to BuildConfig.API_KEY),
                    query
                )
            }
        }catch (e: Exception){
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