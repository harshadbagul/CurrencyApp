package com.andela.currencyapp.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.model.HistoricData
import com.andela.currencyapp.data.netowork.model.Rate
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.repository.CurrencyRepository
import com.andela.currencyapp.data.utils.DateUtils
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val currencyDatabase: CurrencyDatabase
) : ViewModel() {

    val allCurrenciesStateFlow =
        MutableStateFlow<CurrencyState>(CurrencyState.DEFAULT)

    val latestRates =
        MutableStateFlow<CurrencyState>(CurrencyState.DEFAULT)

    val historicRates =
        MutableStateFlow<CurrencyState>(CurrencyState.DEFAULT)


    suspend fun getAllCurrencies() {
        currencyRepository.getAllCurrencies()
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { this.allCurrenciesStateFlow.value = it }
            .launchIn(viewModelScope)

    }

    suspend fun getLatestRates(from: String) {
        currencyRepository.getLatestRates(from)
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { this.latestRates.value = it }
            .launchIn(viewModelScope)
    }

    suspend fun getHistoricRates(from: String, symbols: List<String>) {
        currencyRepository.getHistoricData(from, symbols)
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { this.historicRates.value = it }
            .launchIn(viewModelScope)
    }

    fun getCurrencySymbolList(symbols: Map<String, String>?): List<Currency> {
        val symbolList = ArrayList<Currency>()
        symbols?.map {
            symbolList.add(Currency(symbol = it.key, name = it.value))
        }
        return symbolList
    }

    suspend fun convertRate(fromAmount: String, toSymbol: String, isSwap: Boolean = false): Double {
        var amount = 0.0
        val rates = currencyDatabase.currencyDao.getAllDbCurrencies()
        val rateDao = rates.find { it.symbol == toSymbol }

        if (!isSwap) {
            rateDao?.rate?.let { rate ->
                if (!TextUtils.isEmpty(fromAmount))
                    amount = fromAmount.toDouble() * rate
            }
        } else {
            rateDao?.rate?.let { rate ->
                if (!TextUtils.isEmpty(fromAmount))
                    amount = fromAmount.toDouble() / rate
            }
        }

        return amount
    }

    suspend fun getPopularCurrencyRates(base: String, fromAmount:String, popularCurrencies: List<String>): ArrayList<HistoricData> {
        val rates = currencyDatabase.currencyDao.getAllDbCurrencies()
        val popularCurrenciesRate = rates.filter { it.symbol in popularCurrencies }

        val historicData = ArrayList<HistoricData>()
        popularCurrenciesRate.map {
            historicData.add(
                HistoricData(
                    date = DateUtils.getCurrentDate(),
                    base = base,
                    fromAmount = fromAmount,
                    historicRate = Rate(symbol = it.symbol, dateRate = it.rate ?: 0.0)
                )
            )
        }

        return historicData
    }

    fun convertHistoricData(base:String, data: Any, fromAmount:String): List<HistoricData> {
        val symbolList = ArrayList<HistoricData>()
        val rates = data as? Map<*, *>

        rates?.map {
            symbolList.add( HistoricData(
                date = it.key as String,
                base = base,
                fromAmount = fromAmount ,
                historicRate = getHistoricRate(it.value)
            ))
        }

        return symbolList
    }


    private fun getHistoricRate(data: Any?): Rate {
        val rates = data as Map<*, *>?
        val rate = rates?.map { it }?.get(0)
        return Rate(symbol = rate?.key as String, dateRate = rate.value as Double)
    }


    fun getBarEntries(historicDataList: List<HistoricData>): Pair<ArrayList<BarEntry>, ArrayList<String>> {
        //setup bar entries
        val entries = arrayListOf<BarEntry>()
        historicDataList.take(3).mapIndexed { index, historicData ->
            entries.add(BarEntry(index + 1f, historicData.historicRate.dateRate.toFloat()))
        }

        //setup dates
        val dates = arrayListOf<String>()
        historicDataList.take(4).mapIndexed { index, historicData ->
            dates.add(historicData.date)
        }

        return Pair(entries, dates)
    }
}