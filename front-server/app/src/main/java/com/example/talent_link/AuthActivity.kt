package com.example.talent_link

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.talent_link.ui.Auth.AuthFragment
import com.example.talent_link.ui.Auth.LoginFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    fun openLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.AuthFrame, LoginFragment())
            .addToBackStack(null) // ← 뒤로가기 시 AuthFragment로 복귀 가능
            .commit()
    }
}