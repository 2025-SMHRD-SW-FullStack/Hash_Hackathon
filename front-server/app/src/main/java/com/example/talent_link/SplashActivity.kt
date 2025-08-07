package com.example.talent_link

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.talent_link.util.TokenManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        // 1.5초 후 로그인 상태 확인 -> 이동
        Handler(Looper.getMainLooper()).postDelayed({

            val accessToken = TokenManager.getAccessToken(this)  // ✅ 수정된 부분
            if (!accessToken.isNullOrEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, NoNavActivity::class.java))
            }

            finish()

        }, 1500)
    }

}