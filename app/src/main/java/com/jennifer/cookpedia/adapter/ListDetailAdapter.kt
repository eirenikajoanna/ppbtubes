package com.jennifer.cookpedia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.jennifer.cookpedia.R

class ListDetailAdapter internal constructor (private val context: Context) : BaseAdapter() {
    internal var list = ArrayList<String>()

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemView = convertView
        if(itemView ==  null){
            itemView = LayoutInflater.from(context).inflate(R.layout.item_list_text, parent, false)
        }

        val viewHolder = ViewHolder(itemView as View)
        val text = getItem(position) as String
        viewHolder.bind(text)
        return itemView
    }

    private inner class ViewHolder internal constructor(view: View) {
        private val txtName: TextView = view.findViewById(R.id.text_detail)

        fun bind(text: String) {
            txtName.text = text
        }
    }
}