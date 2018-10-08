package com.nicolettilu.hiddensearchwithrecyclerviewsample

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

class SimpleAdapter(private val arrayOfStrings: List<String>) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>(), Filterable {

    private var copyOfStrings: List<String> = arrayOfStrings.toList()

    override fun getFilter(): Filter =
            object : Filter() {
                override fun performFiltering(p0: CharSequence?): FilterResults {
                    val results = FilterResults()
                    if (p0.isNullOrEmpty()) {
                        results.values = arrayOfStrings
                    } else {
                        copyOfStrings = arrayOfStrings.filter {
                            it.contains(p0 ?: "", true)
                        }
                        results.values = copyOfStrings
                    }
                    return results
                }

                override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                    copyOfStrings = (p1?.values as? List<String>) ?: listOf()
                    notifyDataSetChanged()
                }

            }


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(View.inflate(parent.context, R.layout.simple_adapter, null))
    }

    override fun getItemCount(): Int {
        return copyOfStrings.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(copyOfStrings[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(value: String) {
            this.itemView.findViewById<TextView>(R.id.simple_text).text = value
        }
    }
}