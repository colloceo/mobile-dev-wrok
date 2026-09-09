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
    val totalsByCurrency: List<Pair<String, Double>> = emptyList(),
    val overdueCount: Int = 0
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
                        .map { (currency, group) -> currency to group.sumOf { it.amount } },
                    overdueCount = bills.count {
                        DueDateFormatter.urgencyFor(it.nextDueDateMillis, warningDays) == Urgency.OVERDUE
                    }
                )
            }
        }
    }

    private fun groupByUrgency(bills: List<BillEntity>): List<BillListItem> {
        val result = mutableListOf<BillListItem>()
        var lastBucket: Urgency? = null
        for (bill in bills) {
            val urgency = DueDateFormatter.urgencyFor(bill.nextDueDateMillis, warningDays)
            // Section headers mirror the row pills: overdue is its own bucket,
            // due-soon and upcoming share one "Due soon" header/color.
            val bucket = if (urgency == Urgency.OVERDUE) Urgency.OVERDUE else Urgency.DUE_SOON
            if (bucket != lastBucket) {
                val label = if (bucket == Urgency.OVERDUE) "Overdue" else "Due soon"
                result.add(BillListItem.SectionHeader(label, bucket))
                lastBucket = bucket
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
