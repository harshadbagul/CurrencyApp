package com.andela.currencyapp.data.netowork.service

import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.model.ErrorResponse

sealed class CurrencyState {
    object Loading : CurrencyState()
    data class Success(val response: CurrencyResponse) : CurrencyState()
    data class Error(val error: ErrorResponse?) : CurrencyState()
    object DEFAULT : CurrencyState()

}