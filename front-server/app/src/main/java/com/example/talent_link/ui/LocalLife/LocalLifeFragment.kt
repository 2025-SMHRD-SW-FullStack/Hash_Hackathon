package com.example.talent_link.ui.LocalLife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class LocalLifeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocalLifeAdapter
    private val postList = mutableListOf<LocalPost>()

    private lateinit var jwt: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ 글쓰기 완료 후 결과를 받기 위한 리스너 설정
        parentFragmentManager.setFragmentResultListener(LocalWriteFragment.REQUEST_KEY, this) { _, bundle ->
            val isSuccess = bundle.getBoolean(LocalWriteFragment.BUNDLE_KEY_SUCCESS)
            if (isSuccess) {
                fetchPosts() // 글쓰기 성공 시 목록 새로고침
            }
        }
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

        val btnWrite = view.findViewById<Button>(R.id.btnWrite)
        btnWrite.setOnClickListener {
            // ✅ Activity 대신 Fragment를 띄우도록 수정
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame, LocalWriteFragment())
                .addToBackStack(null)
                .commit()
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