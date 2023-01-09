package com.andela.currencyapp.ui.currency

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andela.currencyapp.MainActivity
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.databinding.FragmentCurrencyBinding
import com.andela.currencyapp.ui.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyFragment : Fragment() {

    private lateinit var binding : FragmentCurrencyBinding
    private var mSymbolList = listOf<Currency>()
    private var isSwap = false

    private val viewModel: CurrencyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDetail.setOnClickListener {
        }

        lifecycleScope.launchWhenStarted {
            viewModel.getAllCurrencies()
        }

        binding.edittextFrom.addTextChangedListener  (
            afterTextChanged = {
                if (it.isNullOrBlank()){
                    binding.edittextFrom.setText("1")
                }
            },
            onTextChanged = { text, _, _, _ ->
                if (isSwap){
                    isSwap = false
                    return@addTextChangedListener
                }
                lifecycleScope.launch {
                    val amount = viewModel.convertRate(
                        fromAmount = text.toString(),
                        toSymbol = binding.spinnerTo.selectedItem.toString())
                    binding.edittextTo.setText(amount.toString())
                }
            },
            beforeTextChanged = {s, start, before, count->
            }
        )



        binding.edittextTo.addTextChangedListener {
        }

        binding.buttonSwap.setOnClickListener {
            isSwap = true
            val from = binding.spinnerFrom.selectedItem.toString()
            val to = binding.spinnerTo.selectedItem.toString()

            val fromIndex = mSymbolList.indexOfFirst { it.symbol == from }
            binding.spinnerTo.setSelection(fromIndex)

            val toIndex = mSymbolList.indexOfFirst { it.symbol == to }
            binding.spinnerFrom.setSelection(toIndex)


            lifecycleScope.launch {
                viewModel.getLatestRates(binding.spinnerFrom.selectedItem.toString())
            }

        }


        binding.spinnerFrom.selected { selectedSymbol ->
            lifecycleScope.launch {
                viewModel.getLatestRates(selectedSymbol)
            }
        }


        binding.spinnerTo.selected { selectedSymbol ->
            lifecycleScope.launch {
                val amount = viewModel.convertRate(
                    fromAmount = binding.edittextFrom.text.toString(),
                    toSymbol = selectedSymbol)

                binding.edittextTo.setText(amount.toString())
            }
        }

    }


    private fun updateCurrencySpinner(symbolList: List<Currency>) {
        mSymbolList = symbolList
        // From spinner update
        val mSymbolFromAdapter = ArrayAdapter(
            activity as Context,
            android.R.layout.simple_dropdown_item_1line,
            symbolList
        )
        binding.spinnerFrom.adapter = mSymbolFromAdapter

        // default selection for from spinner - first item
        binding.spinnerTo.setSelection(0)

        // TO spinner update
        val mSymbolToAdapter = ArrayAdapter(
            activity as Context,
            android.R.layout.simple_dropdown_item_1line,
            symbolList
        )
        binding.spinnerTo.adapter = mSymbolToAdapter

        // default selection for To spinner - second item
        binding.spinnerTo.setSelection(1)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenStarted {
            viewModel.allCurrenciesStateFlow
                .collect{
                    when(it){
                        is CurrencyState.Loading ->{
                            binding.llProgressBar.linearLayout.visibility = View.VISIBLE
                        }
                        is CurrencyState.Success ->{
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val symbolList = viewModel.getCurrencySymbolList(it.response.symbols)
                            updateCurrencySpinner(symbolList)
                        }
                        is CurrencyState.Error ->{
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val errorMessage = it.error?.info
                            (activity as MainActivity).showErrorDialog(message = errorMessage)
                        }
                        else -> { }
                    }
                }
        }



        lifecycleScope.launch {
            viewModel.latestRates.collect {
                when(it){
                    is CurrencyState.Loading ->{
                        binding.llProgressBar.linearLayout.visibility = View.VISIBLE
                    }
                    is CurrencyState.Success ->{
                        binding.llProgressBar.linearLayout.visibility = View.GONE

                        isSwap = false
                        val amount = viewModel.convertRate(
                            fromAmount = binding.edittextFrom.text.toString(),
                            toSymbol = binding.spinnerTo.selectedItem.toString(),
                            isSwap = false
                        )
                        binding.edittextTo.setText(amount.toString())

                    }
                    is CurrencyState.Error ->{
                        binding.llProgressBar.linearLayout.visibility = View.GONE
                        val errorMessage = it.error?.info
                        (activity as MainActivity).showErrorDialog(message = errorMessage)
                    }
                    else -> { }
                }
            }
        }

    }

    private fun Spinner.selected(selection: (selectedItem: String) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selection((view as AppCompatTextView).text.toString())
            }
        }
    }

}