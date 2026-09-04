package com.example.smartexpensemanager.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.repository.CategoryRepository
import com.example.smartexpensemanager.data.repository.TransactionRepository
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Saved : SaveState()
    object Deleted : SaveState()
    data class Error(val reason: String) : SaveState()
}

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userId: Long,
    private val editingId: Long
) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryEntity>>(emptyList())
    val categories: LiveData<List<CategoryEntity>> = _categories

    private val _existing = MutableLiveData<TransactionEntity?>()
    val existing: LiveData<TransactionEntity?> = _existing

    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState

    val isEditing: Boolean get() = editingId != 0L

    init {
        viewModelScope.launch {
            _categories.value = categoryRepository.getAll()
            if (isEditing) {
                _existing.value = transactionRepository.getById(editingId)
            }
        }
    }

    fun save(type: TransactionType, amountText: String, categoryId: Long?, note: String) {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _saveState.value = SaveState.Error("amount")
            return
        }
        if (categoryId == null) {
            _saveState.value = SaveState.Error("category")
            return
        }
        val transaction = TransactionEntity(
            id = editingId,
            userId = userId,
            categoryId = categoryId,
            type = type,
            amount = amount,
            note = note.trim(),
            dateMillis = _existing.value?.dateMillis ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            when (transactionRepository.save(transaction)) {
                is Result.Success -> _saveState.value = SaveState.Saved
                is Result.Error -> _saveState.value = SaveState.Error("database")
            }
        }
    }

    fun delete() {
        val transaction = _existing.value ?: return
        viewModelScope.launch {
            when (transactionRepository.delete(transaction)) {
                is Result.Success -> _saveState.value = SaveState.Deleted
                is Result.Error -> _saveState.value = SaveState.Error("database")
            }
        }
    }
}
