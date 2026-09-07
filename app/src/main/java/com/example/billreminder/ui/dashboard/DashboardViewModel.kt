package com.example.billreminder.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.repository.BillRepository
import com.example.billreminder.data.repository.MarkPaidResult
import com.example.billreminder.util.DueDateFormatter
import com.example.billreminder.util.Result
import com.example.billreminder.util.Urgency
import kotlinx.coroutines.launch

data class DashboardUiState(
    val listItems: List<BillListItem> = emptyList(),
    val hasBills: Boolean = false,
    val totalsByCurrency: List<Pair<String, Double>> = emptyList()
)

sealed class DashboardEvent {
    object NothingToPay : DashboardEvent()
    data class Paid(val billName: String) : DashboardEvent()
    object UndoPerformed : DashboardEvent()
    object DatabaseError : DashboardEvent()
}

class DashboardViewModel(
    private val repository: BillRepository,
    private val userId: Long,
    private val warningDays: Int
) : ViewModel() {

    private val _uiState = MutableLiveData(DashboardUiState())
    val uiState: LiveData<DashboardUiState> = _uiState

    private val _event = MutableLiveData<DashboardEvent?>()
    val event: LiveData<DashboardEvent?> = _event

    private var currentBills: List<BillEntity> = emptyList()
    private var lastMarkPaidResult: MarkPaidResult? = null

    init {
        viewModelScope.launch {
            repository.observeActiveBills(userId).collect { bills ->
                currentBills = bills
                _uiState.value = DashboardUiState(
                    listItems = groupByUrgency(bills),
                    hasBills = bills.isNotEmpty(),
                    totalsByCurrency = bills.groupBy { it.currencyCode }
                        .map { (currency, group) -> currency to group.sumOf { it.amount } }
                )
            }
        }
    }

    private fun groupByUrgency(bills: List<BillEntity>): List<BillListItem> {
        val result = mutableListOf<BillListItem>()
        var lastLabel: String? = null
        for (bill in bills) {
            val urgency = DueDateFormatter.urgencyFor(bill.nextDueDateMillis, warningDays)
            val label = when (urgency) {
                Urgency.OVERDUE -> "Overdue"
                Urgency.DUE_SOON -> "Due soon"
                Urgency.UPCOMING -> "Upcoming"
            }
            if (label != lastLabel) {
                result.add(BillListItem.SectionHeader(label))
                lastLabel = label
            }
            result.add(BillListItem.Row(bill, urgency))
        }
        return result
    }

    fun markTopBillPaid() {
        val bill = currentBills.firstOrNull() ?: run {
            _event.value = DashboardEvent.NothingToPay
            return
        }
        markPaid(bill)
    }

    fun markPaid(bill: BillEntity) {
        viewModelScope.launch {
            when (val result = repository.markPaid(bill)) {
                is Result.Success -> {
                    lastMarkPaidResult = result.data
                    _event.value = DashboardEvent.Paid(bill.name)
                }
                is Result.Error -> _event.value = DashboardEvent.DatabaseError
            }
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            when (repository.delete(bill)) {
                is Result.Success -> Unit
                is Result.Error -> _event.value = DashboardEvent.DatabaseError
            }
        }
    }

    fun undoLastMarkPaid() {
        val result = lastMarkPaidResult ?: return
        lastMarkPaidResult = null
        viewModelScope.launch {
            when (repository.undoMarkPaid(result)) {
                is Result.Success -> _event.value = DashboardEvent.UndoPerformed
                is Result.Error -> _event.value = DashboardEvent.DatabaseError
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
