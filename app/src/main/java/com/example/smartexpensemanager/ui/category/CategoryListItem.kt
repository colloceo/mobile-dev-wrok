package com.example.smartexpensemanager.ui.category

import com.example.smartexpensemanager.data.local.entity.CategoryEntity

sealed class CategoryListItem {
    data class SectionHeader(val label: String) : CategoryListItem()
    data class Chip(val category: CategoryEntity) : CategoryListItem()
}
