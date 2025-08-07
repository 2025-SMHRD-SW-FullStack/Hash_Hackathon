package com.example.talent_link.ui.TalentPost

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_link.data.repository.TalentSellRepository
import com.example.talent_link.ui.TalentBuy.dto.TalentBuyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TalentPostViewModel(
    private val context: Context
) : ViewModel() {

    private val sellRepository = TalentSellRepository(context)
    private val buyRepository = TalentBuyRepository(context)

    fun uploadTalentSell(request: RequestBody, image: MultipartBody.Part?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    sellRepository.uploadTalentSell(request, image)
                }
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun uploadTalentBuy(request: RequestBody, image: MultipartBody.Part?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    buyRepository.uploadTalentBuy(request, image)
                }
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}