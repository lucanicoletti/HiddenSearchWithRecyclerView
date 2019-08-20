package com.nicolettilu.hiddensearchwithrecyclerviewsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Luca Nicoletti
 * © 09/08/2018
 * All rights reserved.
 */

class FragmentDefaultBehaviour : Fragment() {

    companion object {
        const val TAG = "FragmentDefaultBehaviour"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_default, container, false)

        val string = context?.resources?.getString(R.string.lorem_ipsum)
        val arrayOfStrings = string?.split(" ")

        rootView.findViewById<RecyclerView>(R.id.myRecyclerView).layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rootView.findViewById<RecyclerView>(R.id.myRecyclerView).adapter =
            SimpleAdapter(arrayOfStrings.orEmpty())

        return rootView
    }
}