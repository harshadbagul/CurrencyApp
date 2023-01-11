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
import com.andela.currencyapp.data.utils.Utils
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

    // --> CurrencyState.Loading > show loading while calling service
    // --> CurrencyState.Success > Emit values on success
    // --> CurrencyState.Error > Emit error object & handle it in fragment

    private val _allCurrenciesStateFlow = MutableStateFlow<CurrencyState>(CurrencyState.Loading)
    val allCurrenciesStateFlow: StateFlow<CurrencyState>
        get() = _allCurrenciesStateFlow

    private val _latestRates = MutableStateFlow<CurrencyState>(CurrencyState.Loading)
    val latestRates: StateFlow<CurrencyState>
        get() = _latestRates

    private val _historicRates = MutableStateFlow<CurrencyState>(CurrencyState.Loading)
    val historicRates: StateFlow<CurrencyState>
        get() = _historicRates

    private val _loadCurrencyData = MutableStateFlow(true)
    val loadCurrencyData: StateFlow<Boolean>
        get() = _loadCurrencyData

    private val _fromSelection = MutableStateFlow(0)
    val fromSelection: StateFlow<Int>
        get() = _fromSelection

    private val _toSelection = MutableStateFlow(0)
    val toSelection: StateFlow<Int>
        get() = _toSelection


    suspend fun getAllCurrencies() {
        currencyRepository.getAllCurrencies()
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { _allCurrenciesStateFlow.value = it }
            .launchIn(viewModelScope)

    }

    suspend fun getLatestRates(from: String) {
        currencyRepository.getLatestRates(from)
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { _latestRates.value = it }
            .launchIn(viewModelScope)
    }

    suspend fun getHistoricRates(from: String, symbols: List<String>) {
        currencyRepository.getHistoricData(from, symbols)
            .flowOn(Dispatchers.IO)
            .onStart { emit(CurrencyState.Loading) }
            .onEach { _historicRates.value = it }
            .launchIn(viewModelScope)
    }

    fun getCurrencySymbolList(symbols: Map<String, String>?): List<Currency> {
        val symbolList = ArrayList<Currency>()
        symbols?.map {
            symbolList.add(Currency(symbol = it.key, name = it.value))
        }
        return symbolList
    }

    /**
     * Logic for conversion of amount from base to excepted(TO) currency
     */
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

    /**
     * Get popular currency rate from Database
     */
    suspend fun getPopularCurrencyRates(
        base: String,
        fromAmount: String,
        popularCurrencies: List<String>
    ): ArrayList<HistoricData> {
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

    /**
     * Convert historic data to model
     */
    fun convertHistoricData(base: String, data: Any, fromAmount: String): List<HistoricData> {
        val symbolList = ArrayList<HistoricData>()
        val rates = data as? Map<*, *>

        rates?.map {
            symbolList.add(
                HistoricData(
                    date = it.key as String,
                    base = base,
                    fromAmount = fromAmount,
                    historicRate = getHistoricRate(it.value)
                )
            )
        }

        return symbolList
    }


    private fun getHistoricRate(data: Any?): Rate {
        val rates = data as Map<*, *>?
        val rate = rates?.map { it }?.get(0)
        return Rate(symbol = rate?.key as String, dateRate = rate.value as Double)
    }


    /**
     *  prepare bar entries & date's arraylist
     */
    fun getBarEntries(historicDataList: List<HistoricData>): Pair<List<BarEntry>, List<String>> {
        //setup bar entries
        val entries = arrayListOf<BarEntry>()
        val xAxis = arrayListOf<String>()
        val yAxis = arrayListOf<String>()

        List(historicDataList.size) { index ->
            xAxis.add((index * 1f).toString())
        }
        for (i in historicDataList.reversed()) {
            yAxis.add(i.historicRate.dateRate.toString())
        }
        for (i in 0 until xAxis.size){
            entries.add(BarEntry(xAxis[i].toFloat(), yAxis[i].toFloat()))
        }

        //setup dates
        val dates = arrayListOf<String>()
        historicDataList.mapIndexed { index, historicData ->
            dates.add(historicData.date)
        }

        return Pair(entries, dates.sortedWith(Utils.compareByString))
    }

    fun updateFromSelection(fromIndex: Int) {
        _fromSelection.value = fromIndex
    }

    fun updateToSelection(toIndex: Int) {
        _toSelection.value = toIndex
    }

    fun updateReloadFlag(isLoad: Boolean) {
        _loadCurrencyData.value = isLoad
    }
}