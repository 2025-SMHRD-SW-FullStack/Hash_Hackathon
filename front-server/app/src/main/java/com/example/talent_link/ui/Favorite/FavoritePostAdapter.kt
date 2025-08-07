package com.example.talent_link.ui.Favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talent_link.R
import com.example.talent_link.databinding.ItemPostBinding

class FavoritePostAdapter(
    private var favoriteList: List<FavoriteVO>,
    private val onItemClick: (FavoriteVO) -> Unit,
    private val onFavoriteClick: (FavoriteVO, Int) -> Unit
) : RecyclerView.Adapter<FavoritePostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavoriteVO) {
            binding.tvItemTitle.text = item.title
            binding.tvItemLocation.text = item.location
            binding.tvItemPrice.text = item.price

            // 이미지 로딩
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.talentlink_logo) // 로딩 중 이미지
                .error(R.drawable.talentlink_logo)       // 에러 시 이미지
                .into(binding.ivItemImage)


            // 하트 상태 표시
            binding.ivItemFavorite.setImageResource(
                if (item.isFavorite) R.drawable.love_icon else R.drawable.love_icon_outline
            )

            binding.root.setOnClickListener { onItemClick(item) }
            binding.ivItemFavorite.setOnClickListener { onFavoriteClick(item, bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(favoriteList[position])
    }

    override fun getItemCount(): Int = favoriteList.size

    fun updateList(newList: List<FavoriteVO>) {
        favoriteList = newList
        notifyDataSetChanged()
    }
}