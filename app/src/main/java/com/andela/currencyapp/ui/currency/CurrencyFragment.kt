package com.andela.currencyapp.ui.currency

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andela.currencyapp.MainActivity
import com.andela.currencyapp.R
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.utils.Listeners.addTextWatcher
import com.andela.currencyapp.data.utils.Listeners.selected
import com.andela.currencyapp.data.utils.Utils.isNetworkAvailable
import com.andela.currencyapp.data.utils.Utils.showErrorDialog
import com.andela.currencyapp.databinding.FragmentCurrencyBinding
import com.andela.currencyapp.ui.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyFragment : Fragment() {

    private lateinit var binding: FragmentCurrencyBinding
    private var mSymbolList = listOf<Currency>()
    private var isSwap = false

    private val viewModel by activityViewModels<CurrencyViewModel>()


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
            if (isNetworkAvailable(requireContext())){
                viewModel.updateReloadFlag(false)
                navigateToDetailScreen()
            }
        }

        // fetch all currency symbols
        if (viewModel.loadCurrencyData.value){
            getCurrenciesSymbols()
        }

        // watch for text changes in from edittext
        editTextWatcher()

        binding.buttonSwap.setOnClickListener {
            isSwap = true
            binding.spinnerTo.setSelection(viewModel.fromSelection.value)
            binding.spinnerFrom.setSelection(viewModel.toSelection.value)
            lifecycleScope.launch {
                 viewModel.getLatestRates(binding.spinnerFrom.selectedItem.toString())
            }
        }

        // change base currency & get latest rates
        binding.spinnerFrom.selected { selectedSymbol, position ->
            // update from spinner selection in viewmodel
            viewModel.updateFromSelection(position)

            lifecycleScope.launch {
                if (viewModel.loadCurrencyData.value)
                     viewModel.getLatestRates(selectedSymbol)
            }
        }


        // calculate conversion if To spinner value changed
        binding.spinnerTo.selected { selectedSymbol, position ->
            // update to spinner selection in viewmodel
            viewModel.updateToSelection(position)

            lifecycleScope.launch {
                val amount = viewModel.convertRate(
                    fromAmount = binding.edittextFrom.text.toString(),
                    toSymbol = selectedSymbol
                )

                binding.edittextTo.setText(amount.toString())
            }
        }

    }

    /**
     * edit text watcher for from edittext field
     */
    private fun editTextWatcher() {
        binding.edittextFrom.addTextWatcher(activity as Context) { text ->
            if (isSwap) {
                isSwap = false
                return@addTextWatcher
            }
            lifecycleScope.launch {
                val amount = viewModel.convertRate(
                    fromAmount = text,
                    toSymbol = binding.spinnerTo.selectedItem?.toString() ?: ""
                )
                binding.edittextTo.setText(amount.toString())
            }
        }
    }


    private fun getCurrenciesSymbols() {
        lifecycleScope.launchWhenStarted {
            if (isNetworkAvailable(requireContext())) {
                println("######## getAllCurrencies")
                viewModel.getAllCurrencies()
            } else {
                requireContext().showErrorDialog(
                    title = getString(R.string.title_network_alert_dialog),
                    message = getString(R.string.message_network_alert_dialog)
                )
            }
        }
    }


    /**
     *  navigation to detail screen
     */
    private fun navigateToDetailScreen() {
        findNavController().navigate(
            CurrencyFragmentDirections.actionCurrencyFragmentToCurrencyDetailsFragment(
                binding.spinnerFrom.selectedItem.toString(),
                binding.spinnerTo.selectedItem?.toString() ?: "",
                binding.edittextFrom.text.toString()
            )
        )
    }

    /**
     * Update the spinner adapter with currency data
     */
    private fun updateCurrencySpinner(symbolList: List<Currency>) {
        mSymbolList = symbolList
        // From spinner update
        val mSymbolFromAdapter = ArrayAdapter(
            activity as Context,
            android.R.layout.simple_dropdown_item_1line,
            symbolList
        )
        binding.spinnerFrom.adapter = mSymbolFromAdapter
        // default selection for from spinner
        binding.spinnerFrom.setSelection(viewModel.fromSelection.value)


        // TO spinner update
        val mSymbolToAdapter = ArrayAdapter(
            activity as Context,
            android.R.layout.simple_dropdown_item_1line,
            symbolList
        )
        binding.spinnerTo.adapter = mSymbolToAdapter
        // default selection for To spinner
        binding.spinnerTo.setSelection(viewModel.toSelection.value)

    }

    override fun onResume() {
        super.onResume()

        // consume the emitted values for allCurrenciesStateFlow
        // accordingly take action based on CurrencyState
        lifecycleScope.launchWhenStarted {
            viewModel.allCurrenciesStateFlow
                .collect {
                    when (it) {
                        is CurrencyState.Loading -> {
                            binding.llProgressBar.linearLayout.visibility = View.VISIBLE
                        }
                        is CurrencyState.Success -> {
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val symbolList = viewModel.getCurrencySymbolList(it.response.symbols)
                            updateCurrencySpinner(symbolList)
                        }
                        is CurrencyState.Error -> {
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val errorMessage = it.error?.info
                            requireContext().showErrorDialog(message = errorMessage)
                        }
                    }
                }
        }


        // consume the emitted values for latestRates
        // accordingly take action based on CurrencyState
        lifecycleScope.launch {
            viewModel.latestRates.collect {
                when (it) {
                    is CurrencyState.Loading -> {
                        binding.llProgressBar.linearLayout.visibility = View.VISIBLE
                    }
                    is CurrencyState.Success -> {
                        binding.llProgressBar.linearLayout.visibility = View.GONE

                        isSwap = false
                        val amount = viewModel.convertRate(
                            fromAmount = binding.edittextFrom.text.toString(),
                            toSymbol = binding.spinnerTo.selectedItem?.toString() ?: "",
                            isSwap = false
                        )
                        binding.edittextTo.setText(amount.toString())

                    }
                    is CurrencyState.Error -> {
                        binding.llProgressBar.linearLayout.visibility = View.GONE
                        val errorMessage = it.error?.info
                        (activity as MainActivity).showErrorDialog(message = errorMessage)
                    }
                }
            }
        }

    }


}