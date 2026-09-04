package com.example.smartexpensemanager.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensemanager.data.local.entity.TransactionEntity
import com.example.smartexpensemanager.data.local.entity.TransactionWithCategory
import com.example.smartexpensemanager.data.repository.BalanceSummary
import com.example.smartexpensemanager.data.repository.TransactionRepository
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val listItems: List<TransactionListItem> = emptyList(),
    val hasTransactions: Boolean = false,
    val summary: BalanceSummary = BalanceSummary(0.0, 0.0)
)

sealed class LastAction {
    data class Added(val transactionId: Long) : LastAction()
    data class Deleted(val transaction: TransactionEntity) : LastAction()
}

sealed class DashboardEvent {
    object NothingToUndo : DashboardEvent()
    object UndoPerformed : DashboardEvent()
    object DatabaseError : DashboardEvent()
}

class DashboardViewModel(
    private val repository: TransactionRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableLiveData(DashboardUiState())
    val uiState: LiveData<DashboardUiState> = _uiState

    private val _event = MutableLiveData<DashboardEvent?>()
    val event: LiveData<DashboardEvent?> = _event

    private var lastAction: LastAction? = null

    init {
        viewModelScope.launch {
            repository.observeTransactions(userId).combine(repository.observeSummary(userId)) { txns, summary ->
                DashboardUiState(groupByDay(txns), txns.isNotEmpty(), summary)
            }.collect { _uiState.value = it }
        }
    }

    private fun groupByDay(transactions: List<TransactionWithCategory>): List<TransactionListItem> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

        val result = mutableListOf<TransactionListItem>()
        var lastLabel: String? = null
        for (item in transactions) {
            val cal = Calendar.getInstance().apply { timeInMillis = item.transaction.dateMillis }
            val label = when {
                isSameDay(cal, today) -> "Today"
                isSameDay(cal, yesterday) -> "Yesterday"
                else -> dateFormat.format(Date(item.transaction.dateMillis))
            }
            if (label != lastLabel) {
                result.add(TransactionListItem.DateHeader(label))
                lastLabel = label
            }
            result.add(TransactionListItem.Row(item))
        }
        return result
    }

    private fun isSameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    fun notifyAdded(transactionId: Long) {
        lastAction = LastAction.Added(transactionId)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            when (repository.delete(transaction)) {
                is Result.Success -> lastAction = LastAction.Deleted(transaction)
                is Result.Error -> _event.value = DashboardEvent.DatabaseError
            }
        }
    }

    fun undoLastAction() {
        val action = lastAction ?: run {
            _event.value = DashboardEvent.NothingToUndo
            return
        }
        lastAction = null
        viewModelScope.launch {
            val result = when (action) {
                is LastAction.Added -> {
                    val transaction = repository.getById(action.transactionId)
                    if (transaction != null) repository.delete(transaction) else null
                }
                is LastAction.Deleted -> repository.save(action.transaction.copy(id = 0))
            }
            _event.value = when (result) {
                is Result.Error -> DashboardEvent.DatabaseError
                else -> DashboardEvent.UndoPerformed
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
