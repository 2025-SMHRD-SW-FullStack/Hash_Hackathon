package com.example.talent_link.ui.TalentSell

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.talent_link.ui.TalentSell.TalentSellViewModel

class TalentSellViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TalentSellViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TalentSellViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
