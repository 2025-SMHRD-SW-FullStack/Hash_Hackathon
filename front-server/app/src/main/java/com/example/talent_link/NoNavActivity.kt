// main/java/com/example/talent_link/NoNavActivity.kt

package com.example.talent_link

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.talent_link.ui.Auth.AuthFragment
import com.example.talent_link.ui.Auth.LoginFragment
import com.example.talent_link.ui.Auth.SignUpFragment
import com.example.talent_link.ui.LocalLife.LocalWriteFragment
import com.example.talent_link.ui.TalentPost.TalentPostFragment
import com.example.talent_link.util.TokenManager

class NoNavActivity : AppCompatActivity() {

    // 👈 어떤 프래그먼트를 열지 구분하기 위한 상수 추가
    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val TYPE_TALENT_POST = "talent_post"
        const val TYPE_LOCAL_WRITE = "local_write"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val token = TokenManager.getAccessToken(this)
        // 👈 글쓰기는 로그인 상태에서만 가능하므로, 자동 로그인 체크 로직은 그대로 둡니다.
        if (intent.getStringExtra(EXTRA_FRAGMENT_TYPE) == null && !token.isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_nonav)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 👈 Intent 값에 따라 다른 프래그먼트를 열도록 수정
        val fragmentToOpen: Fragment = when (intent.getStringExtra(EXTRA_FRAGMENT_TYPE)) {
            TYPE_TALENT_POST -> TalentPostFragment()
            TYPE_LOCAL_WRITE -> LocalWriteFragment()
            else -> AuthFragment() // 기본값은 인증 화면
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.NoNavFrame, fragmentToOpen)
            .commit()
    }

    fun openSignUpFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.NoNavFrame, SignUpFragment())
            .addToBackStack(null)
            .commit()
    }

    fun openLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.NoNavFrame, LoginFragment())
            .addToBackStack(null)
            .commit()
    }
}