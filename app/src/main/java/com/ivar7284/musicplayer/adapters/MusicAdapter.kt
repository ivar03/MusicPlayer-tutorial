package com.ivar7284.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.musicplayer.databinding.ItemMusicBinding

class MusicAdapter(
    private val musicList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val songPath = musicList[position]
        holder.binding.textViewSongName.text = songPath.substringAfterLast("/")

        holder.itemView.setOnClickListener {
            onItemClick(songPath)
        }
    }

    override fun getItemCount() = musicList.size
}
