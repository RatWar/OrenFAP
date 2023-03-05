package com.besaba.anvarov.orentsd.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.extensions.isExternalStorageReadable
import com.besaba.anvarov.orentsd.room.NomenData
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream

class LoadActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        mAllViewModel =  ViewModelProvider(this).get(AllViewModel::class.java)

        GlobalScope.launch (context = Dispatchers.Main) {
            onLoad()
            delay(10000)
            finish()
        }
    }

    private fun onLoad() {
        val json: String?
        lateinit var mCurrentNomen: NomenData

        if (isExternalStorageReadable()) {
            try {
                mAllViewModel.delNomen()
                @Suppress("DEPRECATION") val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileNomen = "Nomenklatura.json"
                val fileRead = File(path, fileNomen)
                val inputStream: InputStream = fileRead.inputStream()
                json = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val jsonobj = jsonArray.getJSONObject(i)
                    mCurrentNomen = NomenData(
                        jsonobj.getString("Barcode"),
                        jsonobj.getString("Name"),
                        jsonobj.getString("EI"),
                        jsonobj.getInt("MZOO")
                    )
                    mAllViewModel.insertNomen(mCurrentNomen)
                }
                Toast.makeText(applicationContext, "Номенклатура загружена в справочник!", Toast.LENGTH_LONG)
                    .show()
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "Ошибка чтения файла списка номенклатур", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

}
