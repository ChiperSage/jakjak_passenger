package com.jakjak.passenger.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// ---- Toast ----
fun Context.toast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}
fun Fragment.toast(message: String, long: Boolean = false) = requireContext().toast(message, long)

// ---- Snackbar ----
fun View.snack(message: String, long: Boolean = false) {
    Snackbar.make(this, message, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()
}

// ---- View visibility ----
fun View.visible()   { visibility = View.VISIBLE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.gone()      { visibility = View.GONE }

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// ---- Keyboard ----
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
}

// ---- String validation ----
fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPhone(): Boolean =
    this.length >= 10 && this.all { it.isDigit() || it == '+' }

// ---- Geolocation (Haversine) ----
fun haversineDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return 2 * earthRadiusKm * Math.asin(Math.sqrt(a))
}
