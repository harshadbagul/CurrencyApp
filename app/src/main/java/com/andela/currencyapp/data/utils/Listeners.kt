package com.andela.currencyapp.data.utils

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import com.andela.currencyapp.R

object Listeners {

    /**
     *  Extension fun to listen the edittext change events
     */
    fun EditText.addTextWatcher(
        context: Context,
        onTextChanged : (String) -> Unit){
        this.addTextChangedListener  (
            afterTextChanged = {
                if (it.isNullOrBlank()){
                    this.setText(context.getString(R.string.default_text_from))
                }
            },
            onTextChanged = { text, _, _, _ ->
                onTextChanged.invoke(text.toString())
            }
        )
    }


    /**
     *  Extension fun to listen the spinner click event
     */
    fun Spinner.selected(selection: (selectedItem: String, position: Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    selection((it as AppCompatTextView).text.toString(), position)
                }
            }
        }
    }
}