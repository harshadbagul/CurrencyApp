package com.andela.currencyapp.data.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.ArrayList

object Utils {

    fun getPopularCurrencies(): ArrayList<String> {
        val popularCurrencies = arrayListOf<String>()
        val enumeration = Constants.PopularCurrencies.values().toMutableList()
        enumeration.map {
            popularCurrencies.add(it.name)
        }
        return popularCurrencies
    }
}