package com.example.billreminder.ui.currency

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.repository.BillRepository
import com.example.billreminder.data.repository.CurrencyRepository
import com.example.billreminder.util.Result
import kotlinx.coroutines.launch

sealed class CurrencyUiState {
    object Idle : CurrencyUiState()
    object Loading : CurrencyUiState()
    data class Converted(val original: Double, val converted: Double, val currency: String) : CurrencyUiState()
    object NetworkError : CurrencyUiState()
}

class CurrencyViewModel(
    private val billRepository: BillRepository,
    private val currencyRepository: CurrencyRepository,
    private val userId: Long
) : ViewModel() {

    private val _bills = MutableLiveData<List<BillEntity>>(emptyList())
    val bills: LiveData<List<BillEntity>> = _bills

    private val _state = MutableLiveData<CurrencyUiState>(CurrencyUiState.Idle)
    val state: LiveData<CurrencyUiState> = _state

    init {
        viewModelScope.launch {
            billRepository.observeActiveBills(userId).collect { _bills.value = it }
        }
    }

    fun convert(amount: Double, baseCurrency: String, targetCurrency: String) {
        _state.value = CurrencyUiState.Loading
        viewModelScope.launch {
            when (val result = currencyRepository.convert(amount, targetCurrency, baseCurrency)) {
                is Result.Success -> _state.value = CurrencyUiState.Converted(amount, result.data, targetCurrency)
                is Result.Error -> _state.value = CurrencyUiState.NetworkError
            }
        }
    }
}
