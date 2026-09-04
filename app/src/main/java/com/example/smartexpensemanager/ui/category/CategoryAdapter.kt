package com.example.smartexpensemanager.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.databinding.ItemCategoryChipBinding

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_CHIP = 1

class CategoryAdapter(
    private val onClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CategoryListItem.SectionHeader -> VIEW_TYPE_HEADER
        is CategoryListItem.Chip -> VIEW_TYPE_CHIP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view as TextView)
        } else {
            val binding = ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ChipViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryListItem.SectionHeader -> (holder as HeaderViewHolder).bind(item)
            is CategoryListItem.Chip -> (holder as ChipViewHolder).bind(item.category)
        }
    }

    fun spanSizeLookup(spanCount: Int) = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int) = when (getItemViewType(position)) {
            VIEW_TYPE_HEADER -> spanCount
            else -> 1
        }
    }

    class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: CategoryListItem.SectionHeader) {
            textView.text = header.label
        }
    }

    inner class ChipViewHolder(private val binding: ItemCategoryChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryEntity) {
            binding.textName.text = category.name
            binding.textInitial.text = category.name.take(1).uppercase()

            val color = runCatching { Color.parseColor(category.colorHex) }.getOrNull()
                ?: binding.root.context.getColor(R.color.accent)
            binding.avatarBackground.background.setTint(color)

            binding.root.setOnClickListener { onClick(category) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryListItem>() {
            override fun areItemsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean {
                return when {
                    oldItem is CategoryListItem.SectionHeader && newItem is CategoryListItem.SectionHeader ->
                        oldItem.label == newItem.label
                    oldItem is CategoryListItem.Chip && newItem is CategoryListItem.Chip ->
                        oldItem.category.id == newItem.category.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem) = oldItem == newItem
        }
    }
}
