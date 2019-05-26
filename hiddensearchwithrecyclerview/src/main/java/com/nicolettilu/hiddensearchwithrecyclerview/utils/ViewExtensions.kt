package com.nicolettilu.scrolldowntosearchrecyclerview.utils

import android.view.View

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */


/**
 * Set visibility of the view to @link{View.GONE}
 */
fun View.hide() {
    this.visibility = View.GONE
}


/**
 * Set visibility of the view to @link{View.VISIBLE}
 */
fun View.show() {
    this.visibility = View.VISIBLE
}


/**
 * Set visibility of the view to @link{View.INVISIBLE}
 */
fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}


/**
 * Check if the view is visible or not
 * @return visibility == View.VISIBLE
 */
fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}