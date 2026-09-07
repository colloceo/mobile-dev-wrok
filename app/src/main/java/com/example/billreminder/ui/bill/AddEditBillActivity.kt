package com.example.billreminder.ui.bill

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillCategory
import com.example.billreminder.data.local.entity.Recurrence
import com.example.billreminder.databinding.ActivityAddEditBillBinding
import com.example.billreminder.util.BillCurrencies
import com.example.billreminder.util.CategoryStyle
import com.example.billreminder.util.DueDateFormatter
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class AddEditBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBillBinding
    private var selectedDueDateMillis: Long = Calendar.getInstance().timeInMillis
    private var selectedCategory: BillCategory = BillCategory.UTILITY
    private var selectedRecurrence: Recurrence = Recurrence.MONTHLY

    private val editingId: Long by lazy { intent.getLongExtra(EXTRA_BILL_ID, 0L) }

    private val recurrenceLabels = listOf(
        R.string.recurrence_one_time, R.string.recurrence_weekly,
        R.string.recurrence_monthly, R.string.recurrence_yearly
    )

    private val viewModel: BillViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory {
                BillViewModel(app.billRepository, app.sessionManager.loggedInUserId, editingId)
            }
        )[BillViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(
            if (viewModel.isEditing) R.string.edit_bill_title else R.string.add_bill_title
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnDelete.visibility = if (viewModel.isEditing) android.view.View.VISIBLE else android.view.View.GONE

        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, BillCurrencies.codes)
        binding.dropdownCurrency.setAdapter(currencyAdapter)
        binding.dropdownCurrency.setText(app.preferencesManager.preferredCurrency, false)

        categoryChips().forEach { (category, chip, _, _) ->
            chip.setOnClickListener { selectCategory(category) }
        }
        selectCategory(selectedCategory)

        val recurrenceNames = recurrenceLabels.map { getString(it) }
        binding.dropdownRecurrence.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, recurrenceNames))
        binding.dropdownRecurrence.setText(recurrenceNames[2], false) // Monthly
        binding.dropdownRecurrence.setOnItemClickListener { _, _, position, _ ->
            selectedRecurrence = Recurrence.entries[position]
        }

        updateDueDateText()
        binding.btnPickDueDate.setOnClickListener { showDatePicker() }

        binding.btnCall.setOnClickListener { callBiller() }
        binding.btnEmailBiller.setOnClickListener { emailBiller() }

        binding.btnSave.setOnClickListener {
            viewModel.save(
                binding.editName.text?.toString().orEmpty(),
                binding.editAmount.text?.toString().orEmpty(),
                binding.dropdownCurrency.text?.toString()?.takeIf { it.isNotBlank() } ?: BillCurrencies.codes.first(),
                selectedCategory,
                selectedRecurrence,
                selectedDueDateMillis,
                binding.editPhone.text?.toString().orEmpty(),
                binding.editEmail.text?.toString().orEmpty(),
                binding.editNotes.text?.toString().orEmpty()
            )
        }

        binding.btnDelete.setOnClickListener { viewModel.delete() }

        viewModel.existing.observe(this) { bill ->
            if (bill == null) return@observe
            binding.editName.setText(bill.name)
            binding.editAmount.setText(bill.amount.toString())
            binding.dropdownCurrency.setText(bill.currencyCode, false)
            selectCategory(bill.category)
            selectedRecurrence = bill.recurrence
            binding.dropdownRecurrence.setText(getString(recurrenceLabels[bill.recurrence.ordinal]), false)
            selectedDueDateMillis = bill.nextDueDateMillis
            updateDueDateText()
            binding.editPhone.setText(bill.billerPhone.orEmpty())
            binding.editEmail.setText(bill.billerEmail.orEmpty())
            binding.editNotes.setText(bill.notes)
        }

        viewModel.saveState.observe(this) { state ->
            when (state) {
                is BillSaveState.Saved -> {
                    Snackbar.make(binding.root, R.string.bill_saved, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is BillSaveState.Deleted -> {
                    Snackbar.make(binding.root, R.string.bill_deleted, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is BillSaveState.Error -> {
                    val message = when (state.reason) {
                        "name" -> getString(R.string.error_name_required)
                        "amount" -> getString(R.string.error_amount_required)
                        "due_date" -> getString(R.string.error_due_date_required)
                        else -> getString(R.string.error_database)
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun categoryChips() = listOf(
        CategoryChip(BillCategory.UTILITY, binding.chipUtility, binding.chipUtilityBg, binding.chipUtilityIcon),
        CategoryChip(BillCategory.SUBSCRIPTION, binding.chipSubscription, binding.chipSubscriptionBg, binding.chipSubscriptionIcon),
        CategoryChip(BillCategory.RENT, binding.chipRent, binding.chipRentBg, binding.chipRentIcon),
        CategoryChip(BillCategory.LOAN, binding.chipLoan, binding.chipLoanBg, binding.chipLoanIcon),
        CategoryChip(BillCategory.OTHER, binding.chipOther, binding.chipOtherBg, binding.chipOtherIcon)
    )

    private fun selectCategory(category: BillCategory) {
        selectedCategory = category
        categoryChips().forEach { chip ->
            val isSelected = chip.category == category
            val bgColor = if (isSelected) CategoryStyle.colorFor(chip.category) else CategoryStyle.softColorFor(chip.category)
            val iconColor = if (isSelected) R.color.on_accent else CategoryStyle.colorFor(chip.category)
            chip.iconView.setImageResource(CategoryStyle.iconFor(chip.category))
            chip.bgView.background.mutate().setTint(getColor(bgColor))
            chip.iconView.setColorFilter(getColor(iconColor))
        }
    }

    private data class CategoryChip(
        val category: BillCategory,
        val chip: android.view.View,
        val bgView: android.view.View,
        val iconView: android.widget.ImageView
    )

    private fun updateDueDateText() {
        binding.btnPickDueDate.text = DueDateFormatter.formatted(selectedDueDateMillis)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDueDateMillis }
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDueDateMillis = calendar.timeInMillis
                updateDueDateText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun callBiller() {
        val phone = binding.editPhone.text?.toString().orEmpty()
        if (phone.isBlank()) {
            Snackbar.make(binding.root, R.string.error_no_phone, Snackbar.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    private fun emailBiller() {
        val email = binding.editEmail.text?.toString().orEmpty()
        if (email.isBlank()) {
            Snackbar.make(binding.root, R.string.error_no_email, Snackbar.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_biller_subject, binding.editName.text?.toString().orEmpty()))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, R.string.error_no_email_app, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_BILL_ID = "extra_bill_id"
    }
}
