package com.example.billreminder.ui.bill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billreminder.data.local.entity.BillCategory
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.local.entity.Recurrence
import com.example.billreminder.data.repository.BillRepository
import com.example.billreminder.util.Result
import kotlinx.coroutines.launch

sealed class BillSaveState {
    object Idle : BillSaveState()
    object Saved : BillSaveState()
    object Deleted : BillSaveState()
    data class Error(val reason: String) : BillSaveState()
}

class BillViewModel(
    private val repository: BillRepository,
    private val userId: Long,
    private val editingId: Long
) : ViewModel() {

    private val _existing = MutableLiveData<BillEntity?>()
    val existing: LiveData<BillEntity?> = _existing

    private val _saveState = MutableLiveData<BillSaveState>(BillSaveState.Idle)
    val saveState: LiveData<BillSaveState> = _saveState

    val isEditing: Boolean get() = editingId != 0L

    init {
        if (isEditing) {
            viewModelScope.launch {
                _existing.value = repository.getById(editingId)
            }
        }
    }

    fun save(
        name: String,
        amountText: String,
        currencyCode: String,
        category: BillCategory,
        recurrence: Recurrence,
        dueDateMillis: Long?,
        billerPhone: String,
        billerEmail: String,
        notes: String
    ) {
        if (name.isBlank()) {
            _saveState.value = BillSaveState.Error("name")
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _saveState.value = BillSaveState.Error("amount")
            return
        }
        if (dueDateMillis == null) {
            _saveState.value = BillSaveState.Error("due_date")
            return
        }
        val bill = BillEntity(
            id = editingId,
            userId = userId,
            name = name.trim(),
            amount = amount,
            currencyCode = currencyCode,
            category = category,
            recurrence = recurrence,
            nextDueDateMillis = dueDateMillis,
            isActive = _existing.value?.isActive ?: true,
            billerPhone = billerPhone.trim().takeIf { it.isNotBlank() },
            billerEmail = billerEmail.trim().takeIf { it.isNotBlank() },
            notes = notes.trim()
        )
        viewModelScope.launch {
            when (repository.save(bill)) {
                is Result.Success -> _saveState.value = BillSaveState.Saved
                is Result.Error -> _saveState.value = BillSaveState.Error("database")
            }
        }
    }

    fun delete() {
        val bill = _existing.value ?: return
        viewModelScope.launch {
            when (repository.delete(bill)) {
                is Result.Success -> _saveState.value = BillSaveState.Deleted
                is Result.Error -> _saveState.value = BillSaveState.Error("database")
            }
        }
    }
}
