package com.example.smartexpensemanager.ui.transaction

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.mpesa.PendingMpesaStore
import com.example.smartexpensemanager.databinding.ActivityAddEditTransactionBinding
import com.example.smartexpensemanager.util.ViewModelFactory
import com.example.smartexpensemanager.util.app
import com.google.android.material.snackbar.Snackbar

class AddEditTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTransactionBinding
    private var selectedCategoryId: Long? = null
    private var currentType: TransactionType = TransactionType.EXPENSE
    private var categoriesForType: List<CategoryEntity> = emptyList()
    private var allCategories: List<CategoryEntity> = emptyList()

    private val editingId: Long by lazy { intent.getLongExtra(EXTRA_TRANSACTION_ID, 0L) }
    private val prefillAmount: Double? by lazy {
        intent.getDoubleExtra(EXTRA_PREFILL_AMOUNT, -1.0).takeIf { it >= 0 }
    }
    private val prefillType: TransactionType? by lazy {
        intent.getStringExtra(EXTRA_PREFILL_TYPE)?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
    }
    private val prefillNote: String? by lazy { intent.getStringExtra(EXTRA_PREFILL_NOTE) }
    private val pendingMpesaCode: String? by lazy { intent.getStringExtra(EXTRA_PENDING_MPESA_CODE) }

    private val viewModel: TransactionViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory {
                TransactionViewModel(
                    app.transactionRepository,
                    app.categoryRepository,
                    app.sessionManager.loggedInUserId,
                    editingId
                )
            }
        )[TransactionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(
            if (viewModel.isEditing) R.string.edit_transaction_title else R.string.add_transaction_title
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnDelete.visibility = if (viewModel.isEditing) android.view.View.VISIBLE else android.view.View.GONE

        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentType = if (checkedId == binding.btnIncome.id) TransactionType.INCOME else TransactionType.EXPENSE
            refreshCategoryDropdown()
        }
        binding.toggleType.check(binding.btnExpense.id)

        if (!viewModel.isEditing && prefillAmount != null) {
            binding.editAmount.setText(prefillAmount.toString())
            binding.editNote.setText(prefillNote.orEmpty())
            prefillType?.let { type ->
                currentType = type
                binding.toggleType.check(if (type == TransactionType.INCOME) binding.btnIncome.id else binding.btnExpense.id)
            }
        }

        binding.btnSave.setOnClickListener {
            viewModel.save(
                currentType,
                binding.editAmount.text?.toString().orEmpty(),
                selectedCategoryId,
                binding.editNote.text?.toString().orEmpty()
            )
        }

        binding.btnDelete.setOnClickListener { viewModel.delete() }

        viewModel.categories.observe(this) { categories ->
            allCategories = categories
            refreshCategoryDropdown()
        }

        viewModel.existing.observe(this) { transaction ->
            if (transaction == null) return@observe
            currentType = transaction.type
            binding.toggleType.check(if (transaction.type == TransactionType.INCOME) binding.btnIncome.id else binding.btnExpense.id)
            binding.editAmount.setText(transaction.amount.toString())
            binding.editNote.setText(transaction.note)
            selectedCategoryId = transaction.categoryId
            refreshCategoryDropdown()
        }

        viewModel.saveState.observe(this) { state ->
            when (state) {
                is SaveState.Saved -> {
                    pendingMpesaCode?.let { PendingMpesaStore(app).remove(it) }
                    Snackbar.make(binding.root, R.string.transaction_saved, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is SaveState.Deleted -> {
                    Snackbar.make(binding.root, R.string.transaction_deleted, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is SaveState.Error -> {
                    val message = when (state.reason) {
                        "amount" -> getString(R.string.error_amount_required)
                        "category" -> getString(R.string.error_category_required)
                        else -> getString(R.string.error_database)
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun refreshCategoryDropdown() {
        categoriesForType = allCategories.filter { it.type == currentType }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoriesForType.map { it.name })
        binding.dropdownCategory.setAdapter(adapter)

        val selected = categoriesForType.firstOrNull { it.id == selectedCategoryId } ?: categoriesForType.firstOrNull()
        selectedCategoryId = selected?.id
        binding.dropdownCategory.setText(selected?.name.orEmpty(), false)

        binding.dropdownCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categoriesForType.getOrNull(position)?.id
        }
    }

    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_PREFILL_AMOUNT = "extra_prefill_amount"
        const val EXTRA_PREFILL_TYPE = "extra_prefill_type"
        const val EXTRA_PREFILL_NOTE = "extra_prefill_note"
        const val EXTRA_PENDING_MPESA_CODE = "extra_pending_mpesa_code"
    }
}
