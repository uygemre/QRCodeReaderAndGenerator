package com.uygemre.qrcode.extensions

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uygemre.qrcode.R

fun TextInputEditText.checkNull(layout: TextInputLayout): Boolean {
    return if (this.text.isNullOrEmpty()) {
        layout.error = layout.context.resources.getString(R.string.fill_the_blanks)
        layout.setErrorTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
        false
    } else {
        layout.isErrorEnabled = false
        true
    }
}

fun List<TextInputEditText?>?.multipleInputEditText(
    layout1: TextInputLayout,
    layout2: TextInputLayout
): Boolean {
    return if (this?.getOrNull(0)?.text.isNullOrEmpty() && !this?.getOrNull(1)?.text.isNullOrEmpty()) {
        layout1.error = layout1.context.resources.getString(R.string.fill_the_blanks)
        layout1.setErrorTextColor(ColorStateList.valueOf(this?.getOrNull(0)?.resources?.getColor(R.color.red)!!))
        layout2.isErrorEnabled = false
        false
    } else if (!this?.getOrNull(0)?.text.isNullOrEmpty() && this?.getOrNull(1)?.text.isNullOrEmpty()) {
        layout2.error = layout2.context.resources.getString(R.string.fill_the_blanks)
        layout2.setErrorTextColor(ColorStateList.valueOf(this?.getOrNull(1)?.resources?.getColor(R.color.red)!!))
        layout1.isErrorEnabled = false
        false
    } else if (this?.getOrNull(0)?.text.isNullOrEmpty() && this?.getOrNull(1)?.text.isNullOrEmpty()) {
        layout1.error = layout1.context.resources.getString(R.string.fill_the_blanks)
        layout2.error = layout2.context.resources.getString(R.string.fill_the_blanks)
        layout1.setErrorTextColor(ColorStateList.valueOf(this?.getOrNull(0)?.resources?.getColor(R.color.red)!!))
        layout2.setErrorTextColor(ColorStateList.valueOf(this.getOrNull(1)?.resources?.getColor(R.color.red)!!))
        false
    } else {
        layout1.isErrorEnabled = false
        layout2.isErrorEnabled = false
        true
    }
}

fun Context.isNetworkConnected(): Boolean {
    val cm =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return cm!!.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}

fun Context.showNoInternetDialog() {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle(getString(R.string.no_internet_connection))
    builder.setPositiveButton(
        getString(R.string.okay)
    ) { _, i -> }
    builder.show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun <T> Context.openActivity(it: Class<T>, bundle: Bundle) {
    val intent = Intent(this, it)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun dp2px(dp: Int): Float = dp * Resources.getSystem().displayMetrics.density

fun isLollipop() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)