package com.example.smartexpensemanager.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.local.entity.TransactionWithCategory
import com.example.smartexpensemanager.databinding.ItemTransactionBinding
import com.example.smartexpensemanager.util.CurrencyFormatter

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ROW = 1

class TransactionAdapter(
    private val onClick: (TransactionWithCategory) -> Unit,
    private val onLongClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is TransactionListItem.DateHeader -> VIEW_TYPE_HEADER
        is TransactionListItem.Row -> VIEW_TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view as TextView)
        } else {
            val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            RowViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.Row -> (holder as RowViewHolder).bind(item.item)
        }
    }

    class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: TransactionListItem.DateHeader) {
            textView.text = header.label
        }
    }

    inner class RowViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithCategory) {
            binding.textCategoryName.text = item.category.name
            binding.textNote.text = item.transaction.note.ifBlank { item.category.name }

            val isExpense = item.transaction.type == TransactionType.EXPENSE
            val amountText = (if (isExpense) "-" else "+") + CurrencyFormatter.kes(item.transaction.amount)
            binding.textAmount.text = amountText
            binding.textAmount.setTextColor(
                binding.root.context.getColor(if (isExpense) R.color.expense_red else R.color.income_green)
            )

            runCatching { Color.parseColor(item.category.colorHex) }.getOrNull()?.let { color ->
                binding.categoryColorDot.background.setTint(color)
            }

            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TransactionListItem>() {
            override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
                return when {
                    oldItem is TransactionListItem.DateHeader && newItem is TransactionListItem.DateHeader ->
                        oldItem.label == newItem.label
                    oldItem is TransactionListItem.Row && newItem is TransactionListItem.Row ->
                        oldItem.item.transaction.id == newItem.item.transaction.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem) =
                oldItem == newItem
        }
    }
}
