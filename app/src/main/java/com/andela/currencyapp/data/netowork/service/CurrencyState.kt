package com.andela.currencyapp.data.netowork.service

import com.andela.currencyapp.data.netowork.model.CurrencyResponse

sealed class CurrencyState {
    object Loading : CurrencyState()
    data class Success(val response: CurrencyResponse) : CurrencyState()
    data class Error(val error: String) : CurrencyState()
    object DEFAULT : CurrencyState()

}