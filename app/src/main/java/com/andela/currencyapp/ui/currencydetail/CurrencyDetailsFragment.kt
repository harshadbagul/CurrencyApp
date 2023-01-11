package com.andela.currencyapp.ui.currencydetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.andela.currencyapp.R
import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.model.HistoricData
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.utils.Utils
import com.andela.currencyapp.data.utils.Utils.getPopularCurrencies
import com.andela.currencyapp.data.utils.Utils.isNetworkAvailable
import com.andela.currencyapp.data.utils.Utils.showErrorDialog
import com.andela.currencyapp.databinding.FragmentCurrencyDetailsBinding
import com.andela.currencyapp.ui.viewmodel.CurrencyViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class CurrencyDetailsFragment : Fragment() {

    lateinit var binding: FragmentCurrencyDetailsBinding
    private lateinit var mHistoricDataAdapter: CustomAdapter
    private lateinit var mPopularCurrenciesDataAdapter: CustomAdapter

    private val args: CurrencyDetailsFragmentArgs by navArgs()

    private val viewModel by activityViewModels<CurrencyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            val currencies = listOf(args.to)

            if (isNetworkAvailable(requireContext())) {
                //call historic currencies data api
                viewModel.getHistoricRates(args.from, currencies)

            } else {
                requireContext().showErrorDialog(
                    title = getString(R.string.title_network_alert_dialog),
                    message = getString(R.string.message_network_alert_dialog)
                )
            }

        }

    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenStarted {
            viewModel.historicRates.collect {
                    when (it) {
                        is CurrencyState.Loading -> {
                            binding.llProgressBar.linearLayout.visibility = View.VISIBLE
                        }
                        is CurrencyState.Success -> {
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            onSuccess(it.response)
                        }
                        is CurrencyState.Error -> {
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val errorMessage = it.error?.info
                            requireContext().showErrorDialog(message = errorMessage)
                        }
                    }
                }
        }

    }

    /**
     * perform operation on api success response
     */
    private suspend fun onSuccess(response: CurrencyResponse) {
        val historicDataList = viewModel.convertHistoricData(
            base = args.from, data = response.rates, fromAmount = args.amount
        )

        //Update popular currencies in recyclerview
        val popularCurrencies = viewModel.getPopularCurrencyRates(
            base = args.from,
            fromAmount = args.amount,
            popularCurrencies = getPopularCurrencies()
        )

        setPopularCurrencyAdapter(popularCurrencies)
        setAdapter(historicDataList)
        setBarChart(historicDataList)
    }



    /**
     * Set adapter for Historic data
     */
    private fun setAdapter(historicDataList: List<HistoricData>) {
        val list = historicDataList.sortedWith(Utils.compareByHistoricData)

        mHistoricDataAdapter = CustomAdapter(list)
        binding.recyclerviewHistoricData.adapter = mHistoricDataAdapter
    }

    /**
     * Set adapter for popular currencies data
     */
    private fun setPopularCurrencyAdapter(historicDataList: List<HistoricData>) {
        mPopularCurrenciesDataAdapter = CustomAdapter(historicDataList,false)
        binding.recyclerviewPopularData.adapter = mPopularCurrenciesDataAdapter
    }


    /**
     *  Set bar chart UI elements & properties
     */
    private fun setBarChart(historicDataList: List<HistoricData>) {
        val entries = viewModel.getBarEntries(historicDataList)

        // Setting bar Data set
        val barDataSet = BarDataSet(entries.first, args.from).apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple_200)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        // Setting barDataSet to BarData
        val data = BarData(barDataSet).apply {
            barWidth = 0.5f
        }

        binding.barChart.data = data

        // Setting barchart UI element
        binding.barChart.apply {
            setFitBars(true)
            description?.isEnabled = false
            xAxis?.valueFormatter = IndexAxisValueFormatter(entries.second)
            xAxis?.granularity = 1f
            xAxis?.isGranularityEnabled = true
            xAxis?.position = XAxis.XAxisPosition.BOTTOM
        }

        binding.barChart.invalidate()
    }


}