package com.example.billreminder.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.billreminder.data.local.entity.PaymentWithBill
import com.example.billreminder.databinding.ItemPaymentBinding
import com.example.billreminder.util.CategoryStyle
import com.example.billreminder.util.CurrencyFormatter
import com.example.billreminder.util.DueDateFormatter

class PaymentAdapter(
    private val onDelete: (PaymentWithBill) -> Unit
) : ListAdapter<PaymentWithBill, PaymentAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemPaymentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaymentWithBill) {
            val context = binding.root.context
            binding.textBillName.text = item.bill.name
            binding.textPaidDate.text = DueDateFormatter.formatted(item.payment.paidDateMillis)
            binding.textAmount.text = CurrencyFormatter.withCode(item.payment.amountPaid, item.payment.currencyCode)

            binding.categoryIcon.setImageResource(CategoryStyle.iconFor(item.bill.category))
            binding.categoryIcon.setColorFilter(context.getColor(CategoryStyle.colorFor(item.bill.category)))
            binding.categoryIconBg.background.mutate().setTint(context.getColor(CategoryStyle.softColorFor(item.bill.category)))

            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PaymentWithBill>() {
            override fun areItemsTheSame(oldItem: PaymentWithBill, newItem: PaymentWithBill) =
                oldItem.payment.id == newItem.payment.id

            override fun areContentsTheSame(oldItem: PaymentWithBill, newItem: PaymentWithBill) =
                oldItem == newItem
        }
    }
}
