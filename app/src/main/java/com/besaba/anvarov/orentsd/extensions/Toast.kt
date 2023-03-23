package com.besaba.anvarov.orentsd.extensions

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

//inline fun Context.toast(message:()->String){
//    Toast.makeText(this, message() , Toast.LENGTH_LONG).show()
//}
//
//inline fun Fragment.toast(message: () -> String, duration: () -> Int = { Toast.LENGTH_LONG }){
//    Toast.makeText(this.context, message(), duration()).show()
//}

inline fun AppCompatActivity.toast(message: String, duration: () -> Int = { Toast.LENGTH_LONG }){
    Toast.makeText(this.applicationContext, message, duration()).show()
}