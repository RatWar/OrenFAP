package com.besaba.anvarov.orentsd.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.extensions.isExternalStorageReadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream

class DeleteCodesActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_codes)

        mAllViewModel =  ViewModelProvider(this).get(AllViewModel::class.java)

        GlobalScope.launch(context = Dispatchers.Main) {
            onDeleteCodes()
            delay(5000)
            finish()
        }
    }

    private fun onDeleteCodes(){
        val json: String?
        lateinit var mCode: String

        if (isExternalStorageReadable()) {
            try {
                @Suppress("DEPRECATION") val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileCodes = "deleted.json"
                val fileRead = File(path, fileCodes)
                val inputStream: InputStream = fileRead.inputStream()
                json = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    mCode = jsonArray[i].toString()
                    mAllViewModel.deleteCodes(mCode)
                }
                Toast.makeText(applicationContext, "Штрихкоды удалены!", Toast.LENGTH_LONG)
                    .show()
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "Ошибка чтения файла удаления штрихкодов", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}