package com.andela.currencyapp.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.repository.CurrencyRepository
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

}