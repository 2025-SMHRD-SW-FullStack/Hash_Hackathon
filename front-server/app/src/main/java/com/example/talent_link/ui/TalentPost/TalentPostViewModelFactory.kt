package com.example.talent_link.ui.TalentPost

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TalentPostViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TalentPostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TalentPostViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}