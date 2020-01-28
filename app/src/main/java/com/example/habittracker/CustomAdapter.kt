package com.example.habittracker

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val nameList: ArrayList<String>, private val context: Context) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {

    class CustomViewHolder : RecyclerView.ViewHolder {
        companion object {
            var idCount = 0
        }
        private val view: View
        val checkBox: CheckBox
        val infoButton: Button
        val id: Int

        constructor(view: View) : super(view) {
            this.view = view
            checkBox = view.findViewById(R.id.checkBox)
            infoButton = view.findViewById(R.id.infoButton)
            id = idCount
            idCount++
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list_item, parent, false) as View
        val holder = CustomViewHolder(view)
        holder.checkBox.setOnLongClickListener {
            Toast.makeText(parent.context, "I was held!", Toast.LENGTH_SHORT).show()
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.checkBox.text = nameList[position]
        holder.checkBox.isChecked = false
        holder.checkBox.setOnClickListener {
            nameList.remove(holder.checkBox.text.toString())
            notifyItemRemoved(position)
            Toast.makeText(context, "Habit completed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = nameList.size
}