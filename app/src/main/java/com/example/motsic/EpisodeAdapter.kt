package com.example.motsic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.motsic.databinding.ItemEpisodeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpisodeAdapter(private var items: List<Episode>) :
    RecyclerView.Adapter<EpisodeAdapter.VH>() {

    private val df = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    fun update(list: List<Episode>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        val sev = e.severity.coerceIn(1, 5)
        val dots = "\u25CF".repeat(sev) + "\u25CB".repeat(5 - sev)
        holder.binding.title.text = "${e.trigger}  $dots"
        holder.binding.subtitle.text = df.format(Date(e.timestampMs))
        if (e.helped.isBlank()) {
            holder.binding.helped.visibility = View.GONE
        } else {
            holder.binding.helped.visibility = View.VISIBLE
            holder.binding.helped.text = "Helped: ${e.helped}"
        }
    }

    override fun getItemCount() = items.size
}
