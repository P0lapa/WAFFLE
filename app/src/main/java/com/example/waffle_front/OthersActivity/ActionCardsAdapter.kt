package com.example.waffle_front.OthersActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waffle_front.R

class ActionCardsAdapter(
    private val onCardClick: (ContentCard) -> Unit
) : RecyclerView.Adapter<ActionCardsAdapter.ActionViewHolder>() {

    private var cards = listOf<ContentCard>()

    fun submitList(newCards: List<ContentCard>) {
        cards = newCards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actioncard, parent, false)
        return ActionViewHolder(view, onCardClick)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount() = cards.size

    class ActionViewHolder(
        itemView: View,
        private val onCardClick: (ContentCard) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardButton: ImageButton = itemView.findViewById(R.id.actionCardButton)
        private val text: TextView = itemView.findViewById(R.id.situationText)

        fun bind(card: ContentCard) {
            text.text = card.content
            cardButton.setOnClickListener {
                onCardClick(card)
            }
        }
    }
}

