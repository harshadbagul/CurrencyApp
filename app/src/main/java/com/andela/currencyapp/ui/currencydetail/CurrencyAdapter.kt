package com.andela.currencyapp.ui.currencydetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.andela.currencyapp.R
import com.andela.currencyapp.data.netowork.model.HistoricData
import com.andela.currencyapp.databinding.LayoutHistoricDataItemBinding


class CustomAdapter(
	private val mList: List<HistoricData>,
	private val showDate:Boolean = true
	) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

	lateinit var binding : LayoutHistoricDataItemBinding

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		 binding = LayoutHistoricDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		 return ViewHolder(binding.root)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val historicModel = mList[position]

		val amount = historicModel.fromAmount.toDouble() * historicModel.historicRate.dateRate
		val conversionText = historicModel.fromAmount.plus(" ")
			.plus(historicModel.base)
			.plus(" = ")
			.plus(amount)
			.plus(" ")
			.plus(historicModel.historicRate.symbol)

		holder.textViewConversionAmount.text = conversionText
		holder.textViewCurrencyData.text = historicModel.historicRate.dateRate.toString().plus(" ").plus(historicModel.historicRate.symbol)
		if (showDate){
			holder.textViewDate.text = historicModel.date
		}else{
			holder.textViewDate.visibility = View.GONE
		}

	}

	override fun getItemCount(): Int {
		return mList.size
	}

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val textViewDate: AppCompatTextView = itemView.findViewById(R.id.textview_date)
		val textViewCurrencyData: AppCompatTextView = itemView.findViewById(R.id.textview_currency_data)
		val textViewConversionAmount: AppCompatTextView = itemView.findViewById(R.id.textview_amount_conversion)
	}
}
