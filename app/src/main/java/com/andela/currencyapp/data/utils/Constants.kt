package com.andela.currencyapp.data.utils

object Constants {
    const val BASE_URL = "https://api.apilayer.com/fixer/"
    const val API_KEY_NAME = "apiKey"
    const val QUERY_PARAM_BASE = "base"
    const val QUERY_PARAM_SYMBOLS = "symbols"
    const val QUERY_PARAM_START_DATE = "start_date"
    const val QUERY_PARAM_END_DATE = "end_date"

    enum class PopularCurrencies{
        USD,EUR,JPY,GBP,AUD,CAD,CHF,CNH,HKD,NZD,
    }
}