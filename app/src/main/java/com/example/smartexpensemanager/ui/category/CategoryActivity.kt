package com.example.smartexpensemanager.ui.category

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.databinding.ActivityCategoryBinding
import com.example.smartexpensemanager.databinding.DialogCategoryBinding
import com.example.smartexpensemanager.util.BottomNavHelper
import com.example.smartexpensemanager.util.TopLevelDestination
import com.example.smartexpensemanager.util.ViewModelFactory
import com.example.smartexpensemanager.util.app
import com.google.android.material.snackbar.Snackbar

private const val GRID_SPAN_COUNT = 3

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: CategoryAdapter

    private val palette = listOf(
        "#E39898", "#84A9E0", "#82C39C", "#E0B27A", "#AA92CC", "#75BAB3", "#B49B87", "#8E97DA"
    )

    private val viewModel: CategoryViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory { CategoryViewModel(app.categoryRepository) })[CategoryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.CATEGORIES)

        adapter = CategoryAdapter(onClick = { category -> showCategoryDialog(category) })
        val layoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup(GRID_SPAN_COUNT)
        binding.recyclerCategories.layoutManager = layoutManager
        binding.recyclerCategories.adapter = adapter

        binding.fabAdd.setOnClickListener { showCategoryDialog(null) }

        viewModel.listItems.observe(this) { items -> adapter.submitList(items) }
        viewModel.isEmpty.observe(this) { empty ->
            binding.emptyState.visibility = if (empty) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.event.observe(this) { event ->
            val messageRes = when (event) {
                CategoryEvent.Saved -> R.string.transaction_saved
                CategoryEvent.Deleted -> R.string.transaction_deleted
                CategoryEvent.NameRequired -> R.string.error_category_name_required
                CategoryEvent.InUse -> R.string.error_category_in_use
                CategoryEvent.DatabaseError -> R.string.error_database
                null -> return@observe
            }
            Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_SHORT).show()
            viewModel.consumeEvent()
        }
    }

    private fun showCategoryDialog(existing: CategoryEntity?) {
        val dialogBinding = DialogCategoryBinding.inflate(layoutInflater)
        dialogBinding.editCategoryName.setText(existing?.name.orEmpty())
        val isIncome = existing?.type == TransactionType.INCOME
        dialogBinding.toggleType.check(if (isIncome) dialogBinding.btnIncome.id else dialogBinding.btnExpense.id)

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.add_category_title else R.string.edit_category_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val type = if (dialogBinding.toggleType.checkedButtonId == dialogBinding.btnIncome.id) {
                    TransactionType.INCOME
                } else {
                    TransactionType.EXPENSE
                }
                val colorHex = existing?.colorHex ?: palette[categoryCount() % palette.size]
                viewModel.save(
                    existing,
                    dialogBinding.editCategoryName.text?.toString().orEmpty(),
                    type,
                    colorHex
                )
            }
            .setNegativeButton(R.string.cancel, null)

        if (existing != null) {
            builder.setNeutralButton(R.string.btn_delete) { _, _ -> viewModel.delete(existing) }
        }

        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(getColor(R.color.error))
    }

    private fun categoryCount(): Int = adapter.currentList.count { it is CategoryListItem.Chip }
}
