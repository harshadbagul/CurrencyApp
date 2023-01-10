package com.andela.currencyapp.data.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.andela.currencyapp.R
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


    fun Context.showErrorDialog(
                        title:String? = null,
                        message: String? = null)
    {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title ?: this.getString(R.string.default_error_text))
        alertDialog.setMessage(message ?: this.getString(R.string.message_something_went_wrong))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, this.getString(R.string.button_okay)
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectionManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectionManager.getNetworkCapabilities(connectionManager.activeNetwork)
            if (networkCapabilities == null) {
                false
            } else {
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
            }
        } else {
            // below Marshmallow version
            val activeNetwork = connectionManager.activeNetworkInfo
            activeNetwork?.isConnectedOrConnecting == true && activeNetwork.isAvailable
        }
    }

}