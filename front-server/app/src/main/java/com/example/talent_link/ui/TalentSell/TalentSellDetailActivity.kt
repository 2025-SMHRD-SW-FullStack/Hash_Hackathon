package com.example.talent_link.ui.TalentSell

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.talent_link.databinding.ActivityTalentSellDetailBinding
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import kotlinx.coroutines.launch

class TalentSellDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTalentSellDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTalentSellDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getLongExtra("id", -1)
        if (id == -1L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDetail(id)
    }

    private fun loadDetail(id: Long) {
        lifecycleScope.launch {
            try {
                // JWT 필요시 꺼내오기
                val jwt = "Bearer " + com.example.talent_link.util.TokenManager.getToken(this@TalentSellDetailActivity)
                val detail = HomeRetrofitInstance.api.getTalentSellList(jwt).find { it.id == id }
                // ↑ 위 코드는 단일조회 api가 없으면 임시. 단일조회가 있으면 getTalentSellDetail(id)로 바꿔줘!

                if (detail != null) {
                    binding.tvDetailTitle.text = detail.title
                    binding.tvDetailContent.text = detail.description
                    binding.tvDetailPrice.text = "₩${detail.price}"
                    binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
                    // **이미지 없으면 숨김, 있으면 보여주기**
                    val imageUrl = detail.imageUrl
                    if (imageUrl.isNullOrBlank()) {
                        binding.imgDetailPost.visibility = View.GONE
                    } else {
                        binding.imgDetailPost.visibility = View.VISIBLE
                        Glide.with(this@TalentSellDetailActivity).load(imageUrl).into(binding.imgDetailPost)
                    }
                    // **1:1 채팅하기 버튼 클릭 리스너**
                    binding.btnChat.setOnClickListener {
                        Toast.makeText(this@TalentSellDetailActivity, "채팅 기능은 여기에!", Toast.LENGTH_SHORT).show()
                        // 실제로는 채팅방 이동 코드로 변경!
                    }
                    // (옵션) 즐겨찾기 상태/버튼 등 처리

                } else {
                    Toast.makeText(this@TalentSellDetailActivity, "글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TalentSellDetailActivity, "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
