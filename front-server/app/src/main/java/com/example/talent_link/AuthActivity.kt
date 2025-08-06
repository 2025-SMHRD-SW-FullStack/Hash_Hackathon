package com.example.talent_link

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.talent_link.ui.Auth.AuthFragment
import com.example.talent_link.ui.Auth.LoginFragment
import com.example.talent_link.ui.Auth.SignUpFragment

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 자동 로그인 체크
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("accessToken", null)
        if (!token.isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.AuthFrame, AuthFragment())
            .commit()
    }


    fun openSignUpFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.AuthFrame, SignUpFragment())
            .addToBackStack(null)
            .commit()
    }

    fun openLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.AuthFrame, LoginFragment())
            .addToBackStack(null) // ← 뒤로가기 시 AuthFragment로 복귀 가능
            .commit()
    }

}