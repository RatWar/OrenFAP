package com.besaba.anvarov.orentsd.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun showToast(context: Context, @StringRes string : Int, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(context, string, duration).show()
}

inline fun Context.toast(message:()->String){
    Toast.makeText(this, message() , Toast.LENGTH_LONG).show()
}

inline fun Fragment.toast(message: () -> String, duration: () -> Int = { Toast.LENGTH_LONG }){
    Toast.makeText(this.context, message(), duration()).show()
}

inline fun AppCompatActivity.toast(message: String, duration: () -> Int = { Toast.LENGTH_LONG }){
    Toast.makeText(this.applicationContext, message, duration()).show()
}