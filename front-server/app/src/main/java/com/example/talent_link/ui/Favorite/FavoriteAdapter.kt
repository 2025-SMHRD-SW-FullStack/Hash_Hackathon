package com.example.talent_link.ui.Favorite

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R

class FavoriteAdapter(
    val context: Context,
    val favoriteList: ArrayList<FavoriteVO>,
    val onItemClick: (FavoriteVO) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.FavoriteImg)
        val title: TextView = itemView.findViewById(R.id.Favoritetitle)
        val local: TextView = itemView.findViewById(R.id.FavoriteLocal)
        val price: TextView = itemView.findViewById(R.id.Favoriteprice)
        val icon: ImageView = itemView.findViewById(R.id.FavoriteIcon)



        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(favoriteList[pos])
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = favoriteList[position]
        holder.img.setImageResource(item.img)
        holder.title.text = item.title
        holder.local.text = item.local
        holder.price.text = item.price

        // 하트 상태에 따라 이미지 설정
        holder.icon.setImageResource(
            if (item.favorite)R.drawable.love_icon
            else R.drawable.love_icon_outline
        )

        // 클릭 시 하트 상태 토글
        holder.icon.setOnClickListener {
            item.favorite = !item.favorite
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = favoriteList.size
}
