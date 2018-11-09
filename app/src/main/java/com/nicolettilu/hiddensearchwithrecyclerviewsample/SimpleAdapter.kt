package com.nicolettilu.hiddensearchwithrecyclerviewsample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.lang.ref.WeakReference

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

class SimpleAdapter(private val arrayOfStrings: List<String>) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>(), Filterable {

    private var copyOfStrings: List<String> = arrayOfStrings.toList()

    override fun getFilter(): Filter =
            object : Filter() {
                override fun performFiltering(value: CharSequence?): FilterResults {
                    val results = FilterResults()
                    if (value.isNullOrEmpty()) {
                        results.values = arrayOfStrings
                    } else {
                        copyOfStrings = arrayOfStrings.filter {
                            it.contains(value, true)
                        }
                        results.values = copyOfStrings
                    }
                    return results
                }

                override fun publishResults(value: CharSequence?, results: FilterResults?) {
                    copyOfStrings = (results?.values as? List<String>).orEmpty()
                    notifyDataSetChanged()
                }

            }


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val x = WeakReference<Context>(parent.context) as WeakReference<Context>

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