package com.example.talent_link

import ChatListFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.talent_link.Favorite.FavoriteFragment
import com.example.talent_link.Home.HomeFragment
import com.example.talent_link.LocalLife.LocalLifeFragment
import com.example.talent_link.Mypage.MyPageFragment
import com.example.talent_link.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 초기 화면 고정
        supportFragmentManager.beginTransaction().
        replace(R.id.frame, HomeFragment()).commit()

        // 네비 버튼 연결
        binding.nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.btnHome -> replaceFragment(HomeFragment())
                R.id.btnFavorit -> replaceFragment(FavoriteFragment())
                R.id.btnLife -> replaceFragment(LocalLifeFragment())
                R.id.btnMyPage -> replaceFragment(MyPageFragment())
                R.id.btnChat -> replaceFragment(ChatListFragment())
            }
            true
        }


    }
}