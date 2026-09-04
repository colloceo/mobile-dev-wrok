package com.example.smartexpensemanager.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.databinding.ActivityOnboardingBinding
import com.example.smartexpensemanager.ui.auth.LoginActivity
import com.example.smartexpensemanager.util.app

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var dots: List<View>

    private val pages = listOf(
        OnboardingPage(R.drawable.ic_wallet, R.string.onboarding_title_1, R.string.onboarding_body_1),
        OnboardingPage(R.drawable.ic_category, R.string.onboarding_title_2, R.string.onboarding_body_2),
        OnboardingPage(R.drawable.ic_swap, R.string.onboarding_title_3, R.string.onboarding_body_3)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = OnboardingPagerAdapter(pages)
        setUpDots()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                binding.btnNext.setText(
                    if (position == pages.lastIndex) R.string.onboarding_get_started else R.string.onboarding_next
                )
            }
        })

        binding.btnSkip.setOnClickListener { finishOnboarding() }
        binding.btnNext.setOnClickListener {
            val next = binding.viewPager.currentItem + 1
            if (next < pages.size) {
                binding.viewPager.currentItem = next
            } else {
                finishOnboarding()
            }
        }
    }

    private fun setUpDots() {
        dots = pages.indices.map { index ->
            View(this).apply {
                val size = resources.getDimensionPixelSize(R.dimen.onboarding_dot_size)
                val params = android.widget.LinearLayout.LayoutParams(size, size)
                params.marginStart = resources.getDimensionPixelSize(R.dimen.onboarding_dot_spacing)
                params.marginEnd = resources.getDimensionPixelSize(R.dimen.onboarding_dot_spacing)
                layoutParams = params
                setBackgroundResource(R.drawable.bg_dot)
                alpha = if (index == 0) 1f else 0.3f
                binding.dotsContainer.addView(this)
            }
        }
    }

    private fun updateDots(activeIndex: Int) {
        dots.forEachIndexed { index, view -> view.alpha = if (index == activeIndex) 1f else 0.3f }
    }

    private fun finishOnboarding() {
        app.preferencesManager.hasCompletedOnboarding = true
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
