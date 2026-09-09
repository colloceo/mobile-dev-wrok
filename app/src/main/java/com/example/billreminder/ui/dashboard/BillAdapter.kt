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
import com.example.billreminder.util.CategoryStyle
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
            val color = if (header.urgency == Urgency.OVERDUE) R.color.danger else R.color.warn
            textView.setTextColor(textView.context.getColor(color))
        }
    }

    inner class RowViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: BillListItem.Row) {
            val bill = row.bill
            val context = binding.root.context
            binding.textBillName.text = bill.name
            binding.textDueLabel.text = DueDateFormatter.relativeLabel(bill.nextDueDateMillis)
            binding.textAmount.text = CurrencyFormatter.withCode(bill.amount, bill.currencyCode)

            binding.categoryIcon.setImageResource(CategoryStyle.iconFor(bill.category))
            binding.categoryIcon.setColorFilter(context.getColor(R.color.text_secondary))
            binding.categoryIconBg.background.mutate().setTint(context.getColor(R.color.divider))

            // Green is reserved for "paid" confirmations elsewhere (e.g. Payment
            // History) — every not-yet-paid bill here is either overdue (red) or
            // not (amber), regardless of how far out the due date is.
            val (pillSoftColor, pillTextColor) = when (row.urgency) {
                Urgency.OVERDUE -> R.color.danger_soft to R.color.danger
                Urgency.DUE_SOON, Urgency.UPCOMING -> R.color.warn_soft to R.color.warn
            }
            binding.textDueLabel.background.mutate().setTint(context.getColor(pillSoftColor))
            binding.textDueLabel.setTextColor(context.getColor(pillTextColor))

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
