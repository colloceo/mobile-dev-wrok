package com.example.billreminder.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billreminder.data.local.entity.PaymentWithBill
import com.example.billreminder.data.repository.PaymentRepository
import com.example.billreminder.util.Result
import kotlinx.coroutines.launch

class PaymentHistoryViewModel(
    private val repository: PaymentRepository,
    userId: Long
) : ViewModel() {

    private val _payments = MutableLiveData<List<PaymentWithBill>>(emptyList())
    val payments: LiveData<List<PaymentWithBill>> = _payments

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        viewModelScope.launch {
            repository.observeForUser(userId).collect { _payments.value = it }
        }
    }

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch {
            when (repository.delete(paymentId)) {
                is Result.Success -> Unit
                is Result.Error -> _error.value = "database_error"
            }
        }
    }
}
