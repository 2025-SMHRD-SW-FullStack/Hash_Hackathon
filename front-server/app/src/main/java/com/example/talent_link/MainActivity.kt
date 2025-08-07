package com.example.talent_link

import ChatListFragment
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.talent_link.databinding.ActivityMainBinding
import com.example.talent_link.ui.Auth.AuthFragment
import com.example.talent_link.ui.Favorite.FavoriteFragment
import com.example.talent_link.ui.Home.HomeFragment
import com.example.talent_link.ui.LocalLife.LocalLifeFragment
import com.example.talent_link.ui.Mypage.MyPageFragment
import com.example.talent_link.util.TokenManager
import com.google.android.material.snackbar.Snackbar



class MainActivity : AppCompatActivity() {

    fun getFrameLayoutId(): Int = R.id.frame

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }

    private fun isLoggedIn(token: String?): Boolean {
        return !token.isNullOrBlank()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fromLogin = intent.getBooleanExtra("fromLogin", false)
        val token = TokenManager.getAccessToken(this)
        Log.d("자동 로그인", "불러온 토큰: $token")

        val initialFragment = if (fromLogin || isLoggedIn(token)) {
            HomeFragment() // ✅ 로그인한 경우
        } else {
            AuthFragment() // ❗비회원
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, initialFragment)
            .commit()

        // 하단 네비 바 클릭 시 프래그먼트 변경
        binding.nav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.btnHome -> HomeFragment()
                R.id.btnFavorit -> FavoriteFragment()
                R.id.btnLife -> LocalLifeFragment()
                R.id.btnMyPage -> MyPageFragment()
                R.id.btnChat -> ChatListFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, it)
                    .commit()
            }
            true
        }
    }

}