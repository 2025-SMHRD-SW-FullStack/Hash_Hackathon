package com.example.talent_link

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.talent_link.Favorite.FavoriteFragment
import com.example.talent_link.Home.HomeFragment
import com.example.talent_link.LocalLife.LocalLifeFragment
import com.example.talent_link.Mypage.MyPageFragment
import com.example.talent_link.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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
                R.id.btnHome->{
                    supportFragmentManager.beginTransaction().
                    replace(R.id.frame, HomeFragment()).commit()
                }
                R.id.btnFavorit->{
                    supportFragmentManager.beginTransaction().
                    replace(R.id.frame, FavoriteFragment()).commit()
                }
                R.id.btnLife->{
                    supportFragmentManager.beginTransaction().
                    replace(R.id.frame, LocalLifeFragment()).commit()
                }

                // chatFragment
//                R.id.btnChat->{
//                    supportFragmentManager.beginTransaction().
//                    replace(R.id.frame, ()).commit()
//                }

                R.id.btnMyPage->{
                    supportFragmentManager.beginTransaction().
                    replace(R.id.frame, MyPageFragment()).commit()
                }


            }
            true
        }


    }
}