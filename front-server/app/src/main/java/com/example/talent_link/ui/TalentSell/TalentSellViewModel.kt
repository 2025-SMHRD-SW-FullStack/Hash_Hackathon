package com.example.talent_link.ui.Talentsell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_link.data.repository.TalentSellRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class TalentSellViewModel(
    private val repository: TalentSellRepository = TalentSellRepository()
) : ViewModel() {

    fun uploadTalentSell(request: RequestBody, image: MultipartBody.Part?) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.uploadTalentSell(request, image)
                }

                if (response.isSuccessful) {
                    println("📤 업로드 성공: ${response.body()}")
                } else {
                    println("❌ 업로드 실패: ${response.code()} / ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                println("❗ HTTP 예외 발생: ${e.message}")
            } catch (e: Exception) {
                println("❗ 일반 예외 발생: ${e.message}")
            }
        }
    }

}
