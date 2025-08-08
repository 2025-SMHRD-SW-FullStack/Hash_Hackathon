package com.example.talent_link.ui.LocalLife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.NoNavActivity
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class LocalLifeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocalLifeAdapter
    private val postList = mutableListOf<LocalPost>()

    private lateinit var jwt: String

    // 👇 Activity 결과를 받기 위한 런처를 새로 만듭니다.
    private val writePostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 글쓰기 Activity가 성공적으로 끝났을 때 (RESULT_OK) 목록을 새로고침합니다.
        if (result.resultCode == Activity.RESULT_OK) {
            fetchPosts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_local_life, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jwt = "Bearer " + (TokenManager.getAccessToken(requireContext()) ?: "")

        recyclerView = view.findViewById(R.id.LocalRecy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LocalLifeAdapter(postList)
        recyclerView.adapter = adapter

        fetchPosts()

        val btnWrite = view.findViewById<ExtendedFloatingActionButton>(R.id.btnWrite)
        btnWrite.setOnClickListener {
            val intent = Intent(requireContext(), NoNavActivity::class.java)
            intent.putExtra(NoNavActivity.EXTRA_FRAGMENT_TYPE, NoNavActivity.TYPE_LOCAL_WRITE)
            writePostLauncher.launch(intent) // 👈 startActivity 대신 launch 사용
        }
    }

    private fun fetchPosts() {
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.getPosts(jwt)
                if (response.isSuccessful) {
                    postList.clear()
                    postList.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}