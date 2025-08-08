package com.example.talent_link.ui.TalentPost

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_link.data.repository.TalentSellRepository
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import com.example.talent_link.ui.TalentBuy.dto.TalentBuyRepository
import com.example.talent_link.util.TokenManager
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

    fun submitPost(
        mode: String,
        type: String,
        postId: Long,
        request: RequestBody,
        image: MultipartBody.Part?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(context)
                val response = withContext(Dispatchers.IO) {
                    if (mode == "edit") {
                        if (type == "sell") {
                            HomeRetrofitInstance.api.updateTalentSell(postId, jwt, request, image)
                        } else {
                            HomeRetrofitInstance.api.updateTalentBuy(postId, jwt, request, image)
                        }
                    } else { // create
                        if (type == "sell") {
                            sellRepository.uploadTalentSell(request, image)
                        } else {
                            buyRepository.uploadTalentBuy(request, image)
                        }
                    }
                }
                onResult(response.isSuccessful)
                if (!response.isSuccessful) {
                    Log.e("TalentPostViewModel", "API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TalentPostViewModel", "Exception: ", e)
                onResult(false)
            }
        }
    }
}