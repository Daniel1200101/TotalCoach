package com.example.totalcoach.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.totalcoach.PostDetailActivity
import com.example.totalcoach.R
import com.example.totalcoach.models.MediaItem

class MediaAdapter(private val mediaList: List<MediaItem>) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        val mediaCaptionTextView: TextView = itemView.findViewById(R.id.mediaCaptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.media_item, parent, false)
        return MediaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val currentItem = mediaList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.url)
            .centerCrop()
            .into(holder.mediaImageView)

        holder.mediaCaptionTextView.text = currentItem.caption

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("imageUrl", currentItem.url)
                putExtra("caption", currentItem.caption)
                putExtra("description", currentItem.description)
                putExtra("mediaId", currentItem.mediaId) // Ensure mediaId is stored in Firestore
                putExtra("trainerId", currentItem.trainerId)
                putExtra("traineeId", currentItem.traineeId)
                putExtra("mediaType", currentItem.type)

            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount() = mediaList.size
}
