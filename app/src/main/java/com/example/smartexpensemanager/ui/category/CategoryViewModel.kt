package com.example.smartexpensemanager.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.repository.CategoryRepository
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.launch

sealed class CategoryEvent {
    object Saved : CategoryEvent()
    object Deleted : CategoryEvent()
    object NameRequired : CategoryEvent()
    object InUse : CategoryEvent()
    object DatabaseError : CategoryEvent()
}

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val categories: LiveData<List<CategoryEntity>> = repository.observeAll().asLiveData()

    val listItems: LiveData<List<CategoryListItem>> = categories.map { list ->
        val items = mutableListOf<CategoryListItem>()
        val expense = list.filter { it.type == TransactionType.EXPENSE }
        val income = list.filter { it.type == TransactionType.INCOME }
        if (expense.isNotEmpty()) {
            items.add(CategoryListItem.SectionHeader("Expense"))
            items.addAll(expense.map { CategoryListItem.Chip(it) })
        }
        if (income.isNotEmpty()) {
            items.add(CategoryListItem.SectionHeader("Income"))
            items.addAll(income.map { CategoryListItem.Chip(it) })
        }
        items
    }

    val isEmpty: LiveData<Boolean> = categories.map { it.isEmpty() }

    private val _event = MutableLiveData<CategoryEvent?>()
    val event: LiveData<CategoryEvent?> = _event

    fun save(existing: CategoryEntity?, name: String, type: TransactionType, colorHex: String) {
        if (name.isBlank()) {
            _event.value = CategoryEvent.NameRequired
            return
        }
        val category = existing?.copy(name = name.trim(), type = type, colorHex = colorHex)
            ?: CategoryEntity(name = name.trim(), type = type, colorHex = colorHex)
        viewModelScope.launch {
            _event.value = when (repository.save(category)) {
                is Result.Success -> CategoryEvent.Saved
                is Result.Error -> CategoryEvent.DatabaseError
            }
        }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            _event.value = when (val result = repository.delete(category)) {
                is Result.Success -> CategoryEvent.Deleted
                is Result.Error -> if (result.message == "category_in_use") CategoryEvent.InUse else CategoryEvent.DatabaseError
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
