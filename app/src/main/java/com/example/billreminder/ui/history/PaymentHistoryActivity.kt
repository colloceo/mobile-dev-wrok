package com.example.billreminder.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import com.example.billreminder.R
import com.example.billreminder.databinding.ActivityPaymentHistoryBinding
import com.example.billreminder.ui.bill.AddEditBillActivity
import com.example.billreminder.util.BottomNavHelper
import com.example.billreminder.util.TopLevelDestination
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentHistoryBinding

    private val viewModel: PaymentHistoryViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory { PaymentHistoryViewModel(app.paymentRepository, app.sessionManager.loggedInUserId) }
        )[PaymentHistoryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.HISTORY)

        binding.btnEmptyAddBill.setOnClickListener {
            startActivity(Intent(this, AddEditBillActivity::class.java))
        }

        val adapter = PaymentAdapter(onDelete = { payment -> viewModel.deletePayment(payment.payment.id) })
        binding.recyclerPayments.layoutManager = LinearLayoutManager(this)
        binding.recyclerPayments.adapter = adapter

        viewModel.payments.observe(this) { payments ->
            adapter.submitList(payments)
            binding.emptyState.visibility = if (payments.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.recyclerPayments.visibility = if (payments.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                Snackbar.make(binding.root, R.string.error_database, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
