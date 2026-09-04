package com.example.smartexpensemanager.ui.mpesa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.mpesa.MpesaTransaction
import com.example.smartexpensemanager.databinding.ItemPendingMpesaBinding
import com.example.smartexpensemanager.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewMpesaAdapter(
    private val onClick: (MpesaTransaction) -> Unit,
    private val onDismiss: (MpesaTransaction) -> Unit
) : ListAdapter<MpesaTransaction, ReviewMpesaAdapter.ViewHolder>(DIFF) {

    private val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingMpesaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPendingMpesaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MpesaTransaction) {
            binding.textCounterparty.text = item.counterparty
            val typeLabel = binding.root.context.getString(
                if (item.type == TransactionType.INCOME) R.string.type_income else R.string.type_expense
            )
            binding.textMeta.text = "$typeLabel • ${item.code} • ${dateFormat.format(item.dateMillis)}"

            val isExpense = item.type == TransactionType.EXPENSE
            binding.textAmount.text = (if (isExpense) "-" else "+") + CurrencyFormatter.kes(item.amount)
            binding.textAmount.setTextColor(
                binding.root.context.getColor(if (isExpense) R.color.expense_red else R.color.income_green)
            )

            binding.root.setOnClickListener { onClick(item) }
            binding.btnDismiss.setOnClickListener { onDismiss(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MpesaTransaction>() {
            override fun areItemsTheSame(oldItem: MpesaTransaction, newItem: MpesaTransaction) = oldItem.code == newItem.code
            override fun areContentsTheSame(oldItem: MpesaTransaction, newItem: MpesaTransaction) = oldItem == newItem
        }
    }
}
