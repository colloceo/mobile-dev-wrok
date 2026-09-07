package com.example.billreminder.ui.currency

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.databinding.ActivityCurrencyBinding
import com.example.billreminder.util.BillCurrencies
import com.example.billreminder.util.BottomNavHelper
import com.example.billreminder.util.CurrencyFormatter
import com.example.billreminder.util.TopLevelDestination
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app

class CurrencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyBinding
    private var bills: List<BillEntity> = emptyList()
    private var selectedBill: BillEntity? = null

    private val viewModel: CurrencyViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory {
                CurrencyViewModel(app.billRepository, app.currencyRepository, app.sessionManager.loggedInUserId)
            }
        )[CurrencyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.CURRENCY)

        binding.dropdownBill.setOnItemClickListener { _, _, position, _ ->
            selectedBill = bills.getOrNull(position)
            refreshTargetCurrencies()
        }

        binding.btnConvert.setOnClickListener {
            val bill = selectedBill ?: return@setOnClickListener
            val target = binding.dropdownTargetCurrency.text?.toString()?.takeIf { it.isNotBlank() } ?: return@setOnClickListener
            viewModel.convert(bill, target)
        }

        viewModel.bills.observe(this) { list ->
            bills = list
            binding.dropdownBill.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, list.map { it.name }))
            if (selectedBill == null && list.isNotEmpty()) {
                selectedBill = list.first()
                binding.dropdownBill.setText(list.first().name, false)
                refreshTargetCurrencies()
            }
            binding.emptyState.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.formGroup.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state is CurrencyUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.resultCard.visibility = if (state is CurrencyUiState.Converted) android.view.View.VISIBLE else android.view.View.GONE
            binding.textError.visibility = if (state is CurrencyUiState.NetworkError) android.view.View.VISIBLE else android.view.View.GONE

            if (state is CurrencyUiState.Converted) {
                binding.textConverted.text = CurrencyFormatter.withCode(state.converted, state.currency)
            }
        }
    }

    private fun refreshTargetCurrencies() {
        val bill = selectedBill ?: return
        val targets = BillCurrencies.codes.filter { it != bill.currencyCode }
        binding.dropdownTargetCurrency.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, targets))
        binding.dropdownTargetCurrency.setText(targets.firstOrNull().orEmpty(), false)
        binding.textOriginalAmount.text = CurrencyFormatter.withCode(bill.amount, bill.currencyCode)
    }
}
