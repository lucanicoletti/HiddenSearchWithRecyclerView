package com.nicolettilu.scrolldowntosearchrecyclerview.utils

import android.content.Context

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

class Utils {
    companion object {
        fun convertDpToPixel(context: Context, dp: Float): Float {
            val metrics = context.resources.displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return Math.round(px).toFloat()
        }
    }
}