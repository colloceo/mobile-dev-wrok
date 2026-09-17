package com.example.billreminder.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.databinding.ActivityDashboardBinding
import com.example.billreminder.ui.auth.LoginActivity
import com.example.billreminder.ui.bill.AddEditBillActivity
import com.example.billreminder.ui.currency.CurrencyActivity
import com.example.billreminder.ui.history.PaymentHistoryActivity
import com.example.billreminder.util.BottomNavHelper
import com.example.billreminder.util.CategoryStyle
import com.example.billreminder.util.CurrencyFormatter
import com.example.billreminder.util.DueDateFormatter
import com.example.billreminder.util.ShakeDetector
import com.example.billreminder.util.TopLevelDestination
import com.example.billreminder.util.TransitionHelper
import com.example.billreminder.util.Urgency
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var adapter: BillAdapter

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory {
                DashboardViewModel(
                    app.billRepository,
                    app.sessionManager.loggedInUserId,
                    app.preferencesManager.warningDaysBeforeDue
                )
            }
        )[DashboardViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!app.sessionManager.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.HOME)

        val username = app.sessionManager.loggedInUsername.orEmpty()
        binding.textGreeting.text = getString(R.string.dashboard_greeting, username)
        binding.textAvatarInitial.text = username.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()

        adapter = BillAdapter(
            onClick = { bill ->
                startActivity(
                    Intent(this, AddEditBillActivity::class.java)
                        .putExtra(AddEditBillActivity.EXTRA_BILL_ID, bill.id)
                )
                TransitionHelper.forward(this)
            },
            onMarkPaid = { bill -> viewModel.markPaid(bill) },
            onDelete = { bill -> confirmDelete(bill) }
        )
        binding.recyclerBills.layoutManager = LinearLayoutManager(this)
        binding.recyclerBills.adapter = adapter

        binding.actionAddBill.setOnClickListener {
            startActivity(Intent(this, AddEditBillActivity::class.java))
            TransitionHelper.forward(this)
        }
        binding.btnMarkTopPaid.setOnClickListener {
            viewModel.uiState.value?.topBill?.let { viewModel.markPaid(it) }
        }
        binding.actionHistory.setOnClickListener {
            // Same destination as the History tab — switch, don't drill down,
            // so the back stack can't accumulate duplicate Dashboard instances.
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
            finish()
            TransitionHelper.crossFade(this)
        }
        binding.actionCurrency.setOnClickListener {
            startActivity(Intent(this, CurrencyActivity::class.java))
            finish()
            TransitionHelper.crossFade(this)
        }
        binding.btnShare.setOnClickListener { shareSummary() }
        binding.btnBell.setOnClickListener {
            val items = viewModel.uiState.value?.listItems.orEmpty()
            val firstOverdue = items.indexOfFirst { it is BillListItem.Row && it.urgency == Urgency.OVERDUE }
            if (firstOverdue >= 0) {
                binding.recyclerBills.smoothScrollToPosition(firstOverdue)
            } else {
                Snackbar.make(binding.root, R.string.no_overdue_bills, Snackbar.LENGTH_SHORT).show()
            }
        }

        shakeDetector = ShakeDetector(this) {
            runOnUiThread { viewModel.markTopBillPaid() }
        }
        if (!shakeDetector.isAvailable) {
            Snackbar.make(binding.root, R.string.shake_unavailable, Snackbar.LENGTH_LONG).show()
        }

        viewModel.uiState.observe(this) { state ->
            adapter.submitList(state.listItems)
            binding.emptyState.visibility = if (state.hasBills) android.view.View.GONE else android.view.View.VISIBLE
            binding.recyclerBills.visibility = if (state.hasBills) android.view.View.VISIBLE else android.view.View.GONE
            binding.bellBadge.visibility = if (state.overdueCount > 0) android.view.View.VISIBLE else android.view.View.GONE

            binding.nextUpCard.visibility = if (state.topBill != null) android.view.View.VISIBLE else android.view.View.GONE
            binding.totalDueRow.visibility = if (state.hasBills) android.view.View.VISIBLE else android.view.View.GONE

            val topBill = state.topBill
            if (topBill != null) {
                binding.nextUpIcon.setImageResource(CategoryStyle.iconFor(topBill.category))
                binding.nextUpName.text = topBill.name
                binding.nextUpAmount.text = CurrencyFormatter.withCode(topBill.amount, topBill.currencyCode)
                binding.nextUpDueLabel.text = DueDateFormatter.relativeLabel(topBill.nextDueDateMillis)
                val pillColor = when (state.topBillUrgency) {
                    Urgency.OVERDUE -> R.color.danger
                    Urgency.DUE_SOON -> R.color.warn
                    else -> R.color.text_secondary
                }
                binding.nextUpDueLabel.setTextColor(getColor(pillColor))
            }

            val billCount = state.listItems.filterIsInstance<BillListItem.Row>().size
            binding.textTotalDueLabel.text = getString(R.string.total_due_count, billCount)

            binding.totalsContainer.removeAllViews()
            if (state.totalsByCurrency.isEmpty()) {
                binding.textTotalDue.text = CurrencyFormatter.withCode(0.0, app.preferencesManager.preferredCurrency)
            } else {
                val first = state.totalsByCurrency.first()
                binding.textTotalDue.text = CurrencyFormatter.withCode(first.second, first.first)
                state.totalsByCurrency.drop(1).forEach { (currency, amount) ->
                    val extra = android.widget.TextView(this).apply {
                        text = CurrencyFormatter.withCode(amount, currency)
                        setTextColor(getColor(R.color.text_secondary))
                        textSize = 13f
                    }
                    binding.totalsContainer.addView(extra)
                }
            }
        }

        viewModel.event.observe(this) { event ->
            when (event) {
                DashboardEvent.NothingToPay -> {
                    Snackbar.make(binding.root, R.string.nothing_to_pay, Snackbar.LENGTH_SHORT).show()
                }
                is DashboardEvent.Paid -> {
                    Snackbar.make(binding.root, getString(R.string.bill_marked_paid, event.billName), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { viewModel.undoLastMarkPaid() }
                        .show()
                }
                DashboardEvent.UndoPerformed -> {
                    Snackbar.make(binding.root, R.string.undo_performed, Snackbar.LENGTH_SHORT).show()
                }
                DashboardEvent.DatabaseError -> {
                    Snackbar.make(binding.root, R.string.error_database, Snackbar.LENGTH_SHORT).show()
                }
                null -> return@observe
            }
            viewModel.consumeEvent()
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }

    private fun confirmDelete(bill: BillEntity) {
        AlertDialog.Builder(this)
            .setTitle(bill.name)
            .setMessage(R.string.confirm_delete_bill)
            .setPositiveButton(R.string.btn_delete) { _, _ -> viewModel.deleteBill(bill) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun buildSummaryText(): String {
        val state = viewModel.uiState.value ?: return getString(R.string.no_bills)
        val upcoming = state.listItems.filterIsInstance<BillListItem.Row>()
        if (upcoming.isEmpty()) return getString(R.string.no_bills)
        return upcoming.joinToString("\n") { row ->
            "${row.bill.name}: ${CurrencyFormatter.withCode(row.bill.amount, row.bill.currencyCode)} — " +
                DueDateFormatter.relativeLabel(row.bill.nextDueDateMillis)
        }
    }

    private fun shareSummary() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_summary_subject))
            putExtra(Intent.EXTRA_TEXT, buildSummaryText())
        }
        startActivity(Intent.createChooser(intent, getString(R.string.menu_share)))
    }
}
