package com.andela.currencyapp.data.netowork.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CurrencyResponse(
    @PrimaryKey
    val symbol: String,
    val success: Boolean
    )
