package com.example.billreminder.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.databinding.ItemBillBinding
import com.example.billreminder.util.CurrencyFormatter
import com.example.billreminder.util.DueDateFormatter
import com.example.billreminder.util.Urgency

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ROW = 1

class BillAdapter(
    private val onClick: (BillEntity) -> Unit,
    private val onMarkPaid: (BillEntity) -> Unit,
    private val onDelete: (BillEntity) -> Unit
) : ListAdapter<BillListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is BillListItem.SectionHeader -> VIEW_TYPE_HEADER
        is BillListItem.Row -> VIEW_TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view as TextView)
        } else {
            val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            RowViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is BillListItem.SectionHeader -> (holder as HeaderViewHolder).bind(item)
            is BillListItem.Row -> (holder as RowViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: BillListItem.SectionHeader) {
            textView.text = header.label
        }
    }

    inner class RowViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: BillListItem.Row) {
            val bill = row.bill
            binding.textBillName.text = bill.name
            binding.textDueLabel.text = DueDateFormatter.relativeLabel(bill.nextDueDateMillis)
            binding.textAmount.text = CurrencyFormatter.withCode(bill.amount, bill.currencyCode)

            val (dotColor, textColor) = when (row.urgency) {
                Urgency.OVERDUE -> R.color.danger to R.color.danger
                Urgency.DUE_SOON -> R.color.warn to R.color.warn
                Urgency.UPCOMING -> R.color.success to R.color.text_secondary
            }
            binding.urgencyDot.background.setTint(binding.root.context.getColor(dotColor))
            binding.textDueLabel.setTextColor(binding.root.context.getColor(textColor))

            binding.root.setOnClickListener { onClick(bill) }
            binding.btnMarkPaid.setOnClickListener { onMarkPaid(bill) }
            binding.root.setOnLongClickListener {
                onDelete(bill)
                true
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BillListItem>() {
            override fun areItemsTheSame(oldItem: BillListItem, newItem: BillListItem): Boolean {
                return when {
                    oldItem is BillListItem.SectionHeader && newItem is BillListItem.SectionHeader ->
                        oldItem.label == newItem.label
                    oldItem is BillListItem.Row && newItem is BillListItem.Row ->
                        oldItem.bill.id == newItem.bill.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: BillListItem, newItem: BillListItem) = oldItem == newItem
        }
    }
}
