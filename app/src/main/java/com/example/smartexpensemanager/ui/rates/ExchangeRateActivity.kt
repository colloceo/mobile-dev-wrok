package com.example.smartexpensemanager.ui.rates

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.databinding.ActivityExchangeRateBinding
import com.example.smartexpensemanager.util.BottomNavHelper
import com.example.smartexpensemanager.util.CurrencyFormatter
import com.example.smartexpensemanager.util.SupportedCurrencies
import com.example.smartexpensemanager.util.TopLevelDestination
import com.example.smartexpensemanager.util.ViewModelFactory
import com.example.smartexpensemanager.util.app

class ExchangeRateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeRateBinding

    private val viewModel: ExchangeRateViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory {
                ExchangeRateViewModel(app.transactionRepository, app.exchangeRateRepository, app.sessionManager.loggedInUserId)
            }
        )[ExchangeRateViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeRateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.RATES)

        val currencies = SupportedCurrencies.codes
        val defaultCurrency = app.preferencesManager.defaultCurrency.takeIf { it in currencies } ?: currencies.first()
        binding.dropdownCurrency.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, currencies))
        binding.dropdownCurrency.setText(defaultCurrency, false)

        binding.btnConvert.setOnClickListener {
            val currency = binding.dropdownCurrency.text?.toString()?.takeIf { it.isNotBlank() } ?: defaultCurrency
            viewModel.convert(currency)
        }

        viewModel.balance.observe(this) { balance ->
            binding.textBalance.text = CurrencyFormatter.kes(balance)
        }

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state is RatesUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.resultCard.visibility = if (state is RatesUiState.Converted) android.view.View.VISIBLE else android.view.View.GONE
            binding.textError.visibility = if (state is RatesUiState.NetworkError) android.view.View.VISIBLE else android.view.View.GONE

            if (state is RatesUiState.Converted) {
                binding.textConverted.text = getString(R.string.converted_balance_label) + ": " +
                    CurrencyFormatter.withCode(state.converted, state.currency)
            }
        }
    }
}
