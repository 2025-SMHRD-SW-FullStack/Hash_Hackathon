package com.example.talent_link.ui.LocalLife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.talent_link.R

class LocalWriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_write)

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editContent = findViewById<EditText>(R.id.editContent)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val btnAddImg = findViewById<ImageView>(R.id.btnAddImg)

        // 완료(등록) 버튼 클릭
        btnSubmit.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 고정 닉네임/동네
            val nickname = "현진"
            val address = "중흥3동"
            val createdAt = "방금 전"

            val intent = Intent()
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            intent.putExtra("nickname", nickname)
            intent.putExtra("address", address)
            intent.putExtra("createdAt", createdAt)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // 닫기(X) 버튼 클릭 시 액티비티 종료
        btnClose.setOnClickListener {
            finish()
        }

        // 이미지 첨부 버튼 클릭 (지금은 토스트로 임시)
        btnAddImg.setOnClickListener {
            // 추후 이미지 선택 로직 넣을 수 있음
            // 예시: Toast.makeText(this, "이미지 첨부는 추후 지원", Toast.LENGTH_SHORT).show()
        }
    }
}
