package com.nicolettilu.scrolldowntosearchrecyclerview.utils

import android.view.View

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}