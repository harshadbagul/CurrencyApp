package com.andela.currencyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.repository.CurrencyRepository
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class CurrencyViewModelFactory @Inject constructor(
    private val repository: CurrencyRepository,
    private val database: CurrencyDatabase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencyViewModel(repository,database) as T
    }
}