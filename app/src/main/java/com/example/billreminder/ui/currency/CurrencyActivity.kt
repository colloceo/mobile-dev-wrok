package com.example.billreminder.ui.currency

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.databinding.ActivityCurrencyBinding
import com.example.billreminder.ui.bill.AddEditBillActivity
import com.example.billreminder.util.BillCurrencies
import com.example.billreminder.util.BottomNavHelper
import com.example.billreminder.util.TopLevelDestination
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

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

        // Plain AutoCompleteTextView (not wrapped in an ExposedDropdownMenu box)
        // needs an explicit nudge to show its popup on tap since inputType="none"
        // means the text-length filter threshold never gets crossed by typing.
        binding.dropdownTargetCurrency.threshold = 0
        binding.dropdownTargetCurrency.setOnClickListener { binding.dropdownTargetCurrency.showDropDown() }

        binding.btnEmptyAddBill.setOnClickListener {
            startActivity(android.content.Intent(this, AddEditBillActivity::class.java))
        }

        binding.dropdownBill.setOnItemClickListener { _, _, position, _ ->
            selectedBill = bills.getOrNull(position)
            onBillSelected()
        }

        binding.btnConvert.setOnClickListener {
            val bill = selectedBill ?: return@setOnClickListener
            val amount = binding.editFromAmount.text?.toString()?.replace(",", "")?.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                Snackbar.make(binding.root, R.string.error_amount_required, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val target = binding.dropdownTargetCurrency.text?.toString()?.takeIf { it.isNotBlank() } ?: return@setOnClickListener
            viewModel.convert(amount, bill.currencyCode, target)
        }

        viewModel.bills.observe(this) { list ->
            bills = list
            binding.dropdownBill.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, list.map { it.name }))
            if (selectedBill == null && list.isNotEmpty()) {
                selectedBill = list.first()
                binding.dropdownBill.setText(list.first().name, false)
                onBillSelected()
            }
            binding.emptyState.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.formGroup.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state is CurrencyUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.textError.visibility = if (state is CurrencyUiState.NetworkError) android.view.View.VISIBLE else android.view.View.GONE

            if (state is CurrencyUiState.Converted) {
                binding.textConverted.text = String.format(Locale.US, "%,.2f", state.converted)
            }
        }
    }

    private fun onBillSelected() {
        val bill = selectedBill ?: return
        binding.textFromCurrency.text = bill.currencyCode
        binding.editFromAmount.setText(String.format(Locale.US, "%.2f", bill.amount))
        binding.textConverted.text = ""

        val targets = BillCurrencies.codes.filter { it != bill.currencyCode }
        binding.dropdownTargetCurrency.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, targets))
        binding.dropdownTargetCurrency.setText(targets.firstOrNull().orEmpty(), false)
    }
}
