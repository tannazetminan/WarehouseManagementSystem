package com.example.warehousemanagementsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter(private val sessions: List<Session>) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.username.text = session.username
        holder.counter.text = "Counter: ${session.counter}"
        holder.startTime.text = "Start Time: ${session.start_time}"
        holder.endTime.text = "End Time: ${session.end_time ?: "Not ended"}"
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val counter: TextView = itemView.findViewById(R.id.tvCounter)
        val startTime: TextView = itemView.findViewById(R.id.tvStartTime)
        val endTime: TextView = itemView.findViewById(R.id.tvEndTime)
    }
}
