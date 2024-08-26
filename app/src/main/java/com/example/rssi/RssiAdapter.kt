package com.example.rssi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RssiAdapter(private val rssiList: List<String>) :
    RecyclerView.Adapter<RssiAdapter.RssiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return RssiViewHolder(view)
    }

    override fun onBindViewHolder(holder: RssiViewHolder, position: Int) {
        holder.bind(rssiList[position])
    }

    override fun getItemCount(): Int {
        return rssiList.size
    }

    class RssiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(rssi: String) {
            textView.text = rssi
        }
    }
}
