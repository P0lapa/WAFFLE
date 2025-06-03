package com.example.waffle_front.OthersActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.waffle_front.R

class ContentCardDiff : DiffUtil.ItemCallback<ContentCard>() {
    override fun areItemsTheSame(oldItem: ContentCard, newItem: ContentCard): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: ContentCard, newItem: ContentCard): Boolean {
        return oldItem == newItem
    }
}

// 2. Адаптер для “стола” – TableCardsAdapter (item_tablecard.xml)
class TableAdapter : ListAdapter<ContentCard, TableAdapter.TableViewHolder>(ContentCardDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tablecard, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardImage: ImageButton = itemView.findViewById(R.id.tableCardButton)
        private val text: TextView = itemView.findViewById(R.id.tableCardText)

        fun bind(card: ContentCard, position: Int, total: Int) {
            text.text = card.content
            // задаём прозрачность: для последней (position == total - 1) alpha = 1.0f,
            // предыдущая = 0.9f, предыдущая = 0.8f и т.д.
            val alphaValue = 1.0f - ((total - 1 - position) * 0.1f)
            itemView.alpha = alphaValue.coerceIn(0.0f, 1.0f)

            // опционально: анимация появления конкретного item-а (fade/scale)
            if (adapterPosition == total - 1) {
                // новая карточка, можно слегка масштабировать или “поблескать”
                itemView.scaleX = 0.8f
                itemView.scaleY = 0.8f
                itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
        }
    }
}