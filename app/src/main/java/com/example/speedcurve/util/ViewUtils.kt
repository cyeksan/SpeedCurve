package com.example.speedcurve.util

import android.content.Context
import android.widget.Toast


fun Context.getFrameForPosition(position: Int): Int {
    return resources.getIdentifier(
        "f_$position", "drawable",
        packageName
    )
}

fun Context.toast(toastMessage: String) {
    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
}