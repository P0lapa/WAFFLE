package com.example.waffle_front.OthersActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waffle_front.R

class PlayersAdapter : RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder>() {
    private var players = listOf<PlayerData>()

    fun submitList(newPlayers: List<PlayerData>) {
        players = newPlayers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount() = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.player_avatar)
        private val login: TextView = itemView.findViewById(R.id.player_login)

        fun bind(player: PlayerData) {
            login.text = player.login
            Glide.with(itemView)
                .load(R.drawable.player_icon)
                .fitCenter()
                .into(avatar)
        }
    }
}
