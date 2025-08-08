package com.example.talent_link.ui.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talent_link.R
import com.example.talent_link.databinding.ItemPostBinding // 👈 바인딩 클래스 변경
import com.example.talent_link.ui.Home.dto.HomePostUiModel
import com.example.talent_link.ui.Home.dto.PostType

class HomePostAdapter(
    private var postList: List<HomePostUiModel>,
    private val onItemClick: (HomePostUiModel) -> Unit,
    private val onFavoriteClick: (HomePostUiModel, Int) -> Unit
) : RecyclerView.Adapter<HomePostAdapter.PostViewHolder>() {

    // 뷰홀더 내부 바인딩 클래스 변경
    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomePostUiModel) {
            // item_post.xml의 ID에 맞게 데이터 설정
            binding.tvItemTitle.text = item.title
            binding.tvItemLocation.text = "지역 정보" // 모델에 지역 정보가 없으므로 임시 텍스트
            binding.tvItemPrice.text = when (item.type) {
                PostType.SELL -> "₩${item.priceOrBudget}"
                PostType.BUY -> "희망가: ₩${item.priceOrBudget}"
            }

            // Glide를 사용하여 이미지 로딩
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
        // item_post.xml 레이아웃으로 변경
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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