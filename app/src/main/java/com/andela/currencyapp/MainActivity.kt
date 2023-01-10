package com.andela.currencyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    /**
     *   show error for all fragments included in this conatiner
     */
    fun showErrorDialog(title:String?= null,
                        message: String? = getString(R.string.message_network_alert_dialog)){
        MaterialAlertDialogBuilder(applicationContext)
            .setTitle(title ?: getString(R.string.default_error_text))
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_okay)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

}