package com.andela.currencyapp.data.netowork.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class CurrencyResponse(@SerializedName("success") val success: Boolean,
                            @SerializedName("symbols") val symbols: Map<String, String>? = null,
                            @SerializedName("rates") val rates: Any,
                            @SerializedName("error") val errorResponse: ErrorResponse)



@Entity
data class Currency(
    @PrimaryKey
    val symbol: String,
    val name: String? = null,
    val rate: Double? = null){
    override fun toString(): String {
        return symbol
    }
}