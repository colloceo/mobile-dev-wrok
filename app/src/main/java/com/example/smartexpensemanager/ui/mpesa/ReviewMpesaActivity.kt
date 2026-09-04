package com.example.smartexpensemanager.ui.mpesa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensemanager.data.mpesa.MpesaTransaction
import com.example.smartexpensemanager.data.mpesa.PendingMpesaStore
import com.example.smartexpensemanager.databinding.ActivityReviewMpesaBinding
import com.example.smartexpensemanager.ui.transaction.AddEditTransactionActivity

class ReviewMpesaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewMpesaBinding
    private lateinit var adapter: ReviewMpesaAdapter
    private lateinit var store: PendingMpesaStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewMpesaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        store = PendingMpesaStore(this)

        adapter = ReviewMpesaAdapter(
            onClick = { pending ->
                startActivity(
                    Intent(this, AddEditTransactionActivity::class.java)
                        .putExtra(AddEditTransactionActivity.EXTRA_PREFILL_AMOUNT, pending.amount)
                        .putExtra(AddEditTransactionActivity.EXTRA_PREFILL_TYPE, pending.type.name)
                        .putExtra(AddEditTransactionActivity.EXTRA_PREFILL_NOTE, pending.counterparty)
                        .putExtra(AddEditTransactionActivity.EXTRA_PENDING_MPESA_CODE, pending.code)
                )
            },
            onDismiss = { pending ->
                store.remove(pending.code)
                refresh()
            }
        )
        binding.recyclerPending.layoutManager = LinearLayoutManager(this)
        binding.recyclerPending.adapter = adapter

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val pending: List<MpesaTransaction> = store.getAll().sortedByDescending { it.dateMillis }
        adapter.submitList(pending)
        binding.textEmpty.visibility = if (pending.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
