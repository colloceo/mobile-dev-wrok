package com.example.smartexpensemanager.ui.rates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensemanager.data.repository.ExchangeRateRepository
import com.example.smartexpensemanager.data.repository.TransactionRepository
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class RatesUiState {
    object Idle : RatesUiState()
    object Loading : RatesUiState()
    data class Converted(val original: Double, val converted: Double, val currency: String) : RatesUiState()
    object NetworkError : RatesUiState()
}

class ExchangeRateViewModel(
    private val transactionRepository: TransactionRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userId: Long
) : ViewModel() {

    private val _balance = MutableLiveData(0.0)
    val balance: LiveData<Double> = _balance

    private val _state = MutableLiveData<RatesUiState>(RatesUiState.Idle)
    val state: LiveData<RatesUiState> = _state

    init {
        viewModelScope.launch {
            _balance.value = transactionRepository.observeSummary(userId).first().balance
        }
    }

    fun convert(currency: String) {
        val amount = _balance.value ?: 0.0
        _state.value = RatesUiState.Loading
        viewModelScope.launch {
            when (val result = exchangeRateRepository.convert(amount, currency)) {
                is Result.Success -> _state.value = RatesUiState.Converted(amount, result.data, currency)
                is Result.Error -> _state.value = RatesUiState.NetworkError
            }
        }
    }
}
