package com.example.talent_link.ui.LocalLife

import android.app.Activity
import android.content.Intent
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

    private lateinit var userId: String
    private lateinit var jwt: String

    companion object {
        const val WRITE_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_local_life, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = IdManager.getUserId(requireContext()).toString()
        jwt = "Bearer " + (TokenManager.getAccessToken(requireContext()) ?: "")

        recyclerView = view.findViewById(R.id.LocalRecy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LocalLifeAdapter(postList)
        recyclerView.adapter = adapter

        fetchPosts()

        val btnWrite = view.findViewById<Button>(R.id.btnWrite)
        btnWrite.setOnClickListener {
            val intent = Intent(requireContext(), LocalWriteActivity::class.java)
            startActivityForResult(intent, WRITE_REQUEST_CODE)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            fetchPosts() // 글쓰기 이후 목록 새로고침
        }
    }
}
