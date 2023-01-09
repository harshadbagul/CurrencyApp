package com.andela.currencyapp.data.repository

import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.service.CurrencyService
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyService: CurrencyService,
    private val currencyDatabase: CurrencyDatabase
) {

}