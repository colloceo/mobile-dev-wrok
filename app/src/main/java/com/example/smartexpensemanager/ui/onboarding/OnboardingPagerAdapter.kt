package com.example.smartexpensemanager.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensemanager.databinding.ItemOnboardingPageBinding

data class OnboardingPage(val iconRes: Int, val titleRes: Int, val descriptionRes: Int)

class OnboardingPagerAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.imageIcon.setImageResource(page.iconRes)
        holder.binding.textTitle.setText(page.titleRes)
        holder.binding.textDescription.setText(page.descriptionRes)
    }

    override fun getItemCount(): Int = pages.size

    class ViewHolder(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root)
}
