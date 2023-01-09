package com.andela.currencyapp.data.netowork.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(@SerializedName("code") val code: String,
                         @SerializedName("info") val info: String)