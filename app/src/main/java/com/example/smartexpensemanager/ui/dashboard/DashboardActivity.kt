package com.example.smartexpensemanager.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.mpesa.PendingMpesaStore
import com.example.smartexpensemanager.databinding.ActivityDashboardBinding
import com.example.smartexpensemanager.ui.auth.LoginActivity
import com.example.smartexpensemanager.ui.mpesa.ReviewMpesaActivity
import com.example.smartexpensemanager.ui.transaction.AddEditTransactionActivity
import com.example.smartexpensemanager.util.BottomNavHelper
import com.example.smartexpensemanager.util.CurrencyFormatter
import com.example.smartexpensemanager.util.ShakeDetector
import com.example.smartexpensemanager.util.TopLevelDestination
import com.example.smartexpensemanager.util.ViewModelFactory
import com.example.smartexpensemanager.util.app
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var adapter: TransactionAdapter

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory { DashboardViewModel(app.transactionRepository, app.sessionManager.loggedInUserId) }
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
        setSupportActionBar(binding.toolbar)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.HOME)

        val username = app.sessionManager.loggedInUsername.orEmpty()
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greetingRes = when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
        binding.toolbar.title = getString(R.string.dashboard_greeting, getString(greetingRes), username)
        binding.toolbar.subtitle = getString(R.string.dashboard_subtitle)

        adapter = TransactionAdapter(
            onClick = { item ->
                startActivity(
                    Intent(this, AddEditTransactionActivity::class.java)
                        .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, item.transaction.id)
                )
            },
            onLongClick = { item ->
                viewModel.deleteTransaction(item.transaction)
                Snackbar.make(binding.root, R.string.transaction_deleted, Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerTransactions.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTransactionActivity::class.java))
        }

        binding.btnReviewMpesa.setOnClickListener {
            startActivity(Intent(this, ReviewMpesaActivity::class.java))
        }

        shakeDetector = ShakeDetector(this) {
            runOnUiThread { viewModel.undoLastAction() }
        }
        if (!shakeDetector.isAvailable) {
            Snackbar.make(binding.root, R.string.shake_unavailable, Snackbar.LENGTH_LONG).show()
        }

        viewModel.uiState.observe(this) { state ->
            adapter.submitList(state.listItems)
            binding.emptyState.visibility = if (state.hasTransactions) android.view.View.GONE else android.view.View.VISIBLE
            binding.recyclerTransactions.visibility = if (state.hasTransactions) android.view.View.VISIBLE else android.view.View.GONE
            binding.textBalance.text = CurrencyFormatter.kes(state.summary.balance)
            binding.textIncome.text = CurrencyFormatter.kes(state.summary.income)
            binding.textExpense.text = CurrencyFormatter.kes(state.summary.expense)
        }

        viewModel.event.observe(this) { event ->
            val messageRes = when (event) {
                DashboardEvent.NothingToUndo -> R.string.nothing_to_undo
                DashboardEvent.UndoPerformed -> R.string.shake_undo_toast
                DashboardEvent.DatabaseError -> R.string.error_database
                null -> return@observe
            }
            Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_SHORT).show()
            viewModel.consumeEvent()
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
        refreshMpesaBanner()
    }

    private fun refreshMpesaBanner() {
        val count = PendingMpesaStore(app).getAll().size
        binding.mpesaBanner.visibility = if (count > 0) android.view.View.VISIBLE else android.view.View.GONE
        if (count > 0) {
            binding.textMpesaBannerBody.text = getString(R.string.mpesa_banner_body, count)
        }
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareSummary()
                true
            }
            R.id.action_email -> {
                emailSummary()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buildSummaryText(): String {
        val state = viewModel.uiState.value ?: return getString(R.string.no_transactions)
        return getString(R.string.balance_label) + ": " + CurrencyFormatter.kes(state.summary.balance) +
            "\n" + getString(R.string.income_label) + ": " + CurrencyFormatter.kes(state.summary.income) +
            "\n" + getString(R.string.expense_label) + ": " + CurrencyFormatter.kes(state.summary.expense)
    }

    private fun shareSummary() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_summary_subject))
            putExtra(Intent.EXTRA_TEXT, buildSummaryText())
        }
        startActivity(Intent.createChooser(intent, getString(R.string.menu_share)))
    }

    private fun emailSummary() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_summary_subject))
            putExtra(Intent.EXTRA_TEXT, buildSummaryText())
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_title)))
        } else {
            Snackbar.make(binding.root, R.string.error_no_email_app, Snackbar.LENGTH_SHORT).show()
        }
    }
}
