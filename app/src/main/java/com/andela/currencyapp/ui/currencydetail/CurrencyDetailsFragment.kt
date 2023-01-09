package com.andela.currencyapp.ui.currencydetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.andela.currencyapp.MainActivity
import com.andela.currencyapp.R
import com.andela.currencyapp.data.netowork.model.HistoricData
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.utils.Utils.getPopularCurrencies
import com.andela.currencyapp.databinding.FragmentCurrencyDetailsBinding
import com.andela.currencyapp.ui.viewmodel.CurrencyViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CurrencyDetailsFragment : Fragment() {

    lateinit var binding: FragmentCurrencyDetailsBinding
    private lateinit var mHistoricDataAdapter: CustomAdapter
    private lateinit var mPopularCurrenciesDataAdapter: CustomAdapter

    private val args: CurrencyDetailsFragmentArgs by navArgs()

    private val viewModel: CurrencyViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val currencies = listOf(args.to)

            //call historic currencies data api
            viewModel.getHistoricRates(args.from, currencies)

            //Update popular currencies in recyclerview
            val historicDataList = viewModel.getPopularCurrencyRates(
                    base = args.from,
                    fromAmount = args.amount,
                    popularCurrencies = getPopularCurrencies())
            setPopularCurrencyAdapter(historicDataList)
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
                            val historicDataList = viewModel.convertHistoricData(
                                base = args.from, data = it.response.rates, fromAmount = args.amount
                            )
                            setAdapter(historicDataList)
                            setBarChart(historicDataList)
                        }
                        is CurrencyState.Error -> {
                            binding.llProgressBar.linearLayout.visibility = View.GONE
                            val errorMessage = it.error?.info
                            (activity as MainActivity).showErrorDialog(message = errorMessage)
                        }
                        else -> {}
                    }
                }
        }

    }


    private fun setAdapter(historicDataList: List<HistoricData>) {
        historicDataList.reversed()
        mHistoricDataAdapter = CustomAdapter(historicDataList)
        binding.recyclerviewHistoricData.adapter = mHistoricDataAdapter
    }

    private fun setPopularCurrencyAdapter(historicDataList: List<HistoricData>) {
        mPopularCurrenciesDataAdapter = CustomAdapter(historicDataList,false)
        binding.recyclerviewPopularData.adapter = mPopularCurrenciesDataAdapter
    }


    private fun setBarChart(historicDataList: List<HistoricData>) {
        val entries = viewModel.getBarEntries(historicDataList)

        val barDataSet = BarDataSet(entries.first, args.from).apply {
            color = ContextCompat.getColor(activity as Context, R.color.purple_200)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val data = BarData(barDataSet).apply {
            barWidth = 0.5f
        }

        binding.barChart.data = data

        binding.barChart.apply {
            setFitBars(true)
            description?.isEnabled = false
            xAxis?.valueFormatter = IndexAxisValueFormatter(entries.second)
            xAxis?.granularity = 0.5f
            xAxis?.isGranularityEnabled = true
            xAxis?.position = XAxis.XAxisPosition.BOTTOM
        }

        binding.barChart.invalidate()
    }


}