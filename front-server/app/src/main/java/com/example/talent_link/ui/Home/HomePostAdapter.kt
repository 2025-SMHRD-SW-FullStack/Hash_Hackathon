package com.example.talent_link.ui.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.databinding.ItemHomePostBinding
import com.example.talent_link.ui.Home.dto.HomePostUiModel
import com.example.talent_link.ui.Home.dto.PostType

class HomePostAdapter(
    private var postList: List<HomePostUiModel>,
    private val onItemClick: (HomePostUiModel) -> Unit,
    private val onFavoriteClick: (HomePostUiModel, Int) -> Unit
) : RecyclerView.Adapter<HomePostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemHomePostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomePostUiModel) {
            binding.tvTitle.text = item.title
            binding.tvContent.text = item.content
            binding.tvPrice.text = when (item.type) {
                PostType.SELL -> "₩${item.priceOrBudget}"
                PostType.BUY -> "희망가: ₩${item.priceOrBudget}"
            }

            // 하트 상태 표시
            binding.imgFavorite.setImageResource(
                if (item.isFavorite) R.drawable.love_icon else R.drawable.love_icon_outline
            )

            binding.root.setOnClickListener { onItemClick(item) }
            binding.imgFavorite.setOnClickListener { onFavoriteClick(item, bindingAdapterPosition) }
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

    fun updateList(newList: List<HomePostUiModel>) {
        postList = newList
        notifyDataSetChanged()
    }
}
