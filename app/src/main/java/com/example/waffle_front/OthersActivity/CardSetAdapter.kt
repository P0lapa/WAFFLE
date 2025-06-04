package com.example.waffle_front.OthersActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waffle_front.R

data class CardSet(
    val name: String,
    var isSelected: Boolean
)

class CardSetAdapter(
    private val cardSets: List<CardSet>,
    private val onSwitchChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<CardSetAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.cardSetName)
        val switchSelect: Switch = itemView.findViewById(R.id.cardSetSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_set, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardSet = cardSets[position]
        holder.nameTextView.text = cardSet.name
        holder.switchSelect.isChecked = cardSet.isSelected

        holder.switchSelect.setOnCheckedChangeListener { _, isChecked ->
            cardSet.isSelected = isChecked
            onSwitchChanged(position, isChecked)
        }
    }

    override fun getItemCount() = cardSets.size
}

