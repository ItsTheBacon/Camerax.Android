package com.bacon.camerax.android.presentation.extensions

import android.widget.TextView
import androidx.annotation.StringRes

fun TextView.setTextRes(@StringRes resId: Int) {
    text = context.getString(resId)
}
