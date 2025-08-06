package com.example.talent_link.ui.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.databinding.ItemHomePostBinding

class HomePostAdapter(private val postList: List<String>) :
    RecyclerView.Adapter<HomePostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemHomePostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvTitle.text = title
            binding.tvContent.text = "내용 없음"
            binding.tvPrice.text = "₩0"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemHomePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}

