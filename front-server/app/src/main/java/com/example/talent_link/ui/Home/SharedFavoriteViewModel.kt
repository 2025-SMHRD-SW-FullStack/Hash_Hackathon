package com.example.talent_link.ui.Home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedFavoriteViewModel : ViewModel() {
    // Pair<postId, type> 형식 (예: 123L, "sell")
    private val _favoriteChanged = MutableLiveData<Pair<Long, String>?>()
    val favoriteChanged: LiveData<Pair<Long, String>?> = _favoriteChanged

    fun notifyFavoriteChanged(postId: Long, type: String) {
        _favoriteChanged.value = Pair(postId, type)
    }

    fun reset() {
        _favoriteChanged.value = null
    }
}