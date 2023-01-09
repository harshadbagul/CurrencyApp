package com.andela.currencyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val currencyDatabase: CurrencyDatabase
) : ViewModel() {


}