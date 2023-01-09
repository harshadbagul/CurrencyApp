package com.andela.currencyapp.data.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"

    fun getCurrentDate(): String {
        val date = System.currentTimeMillis()
        val datetLong = Date(date)
        val sdfDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(datetLong)

        return sdfDate ?: ""
    }

    fun getDateBeforeDays(beforeDays: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, beforeDays)
        val sdfDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.time)

        return sdfDate ?: ""
    }


}