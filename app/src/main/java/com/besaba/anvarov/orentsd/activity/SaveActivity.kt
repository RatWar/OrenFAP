package com.besaba.anvarov.orentsd.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.extensions.addScan
import com.besaba.anvarov.orentsd.extensions.writeJson
import com.besaba.anvarov.orentsd.extensions.writeJsonFinish
import org.json.JSONObject

class SaveActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)

        mAllViewModel =  ViewModelProvider(this).get(AllViewModel::class.java)

        onSave()
        finish()
//        GlobalScope.launch(context = Dispatchers.Main) {
//            onSave()
//            delay(5000)
//            finish()
//        }
    }

    private fun onSave() {
        val scans = mutableListOf<JSONObject>()
        var numberDoc: Int = -1
        var buf: Int = -1
        var dateDoc = ""
        var bufDoc = ""
        val all = mAllViewModel.getAll()
        if (all!!.isNotEmpty()) {
            for (it in all) {
                if ((it.numDoc == numberDoc) or (numberDoc == -1)) { // продолжаю заполнять массив
                    scans.add(addScan(it))
                    buf = it.numDoc
                    bufDoc = it.dateTime
                } else {                      // записываю документ и начинаю заполнять снова массив
                    if (numberDoc == -1) {
                        numberDoc = buf
                        dateDoc = bufDoc
                    }
                    writeJson(scans.toString(), numberDoc, dateDoc, scans.size)
                    numberDoc = it.numDoc
                    dateDoc = it.dateTime
                    scans.clear()
                    scans.add(addScan(it))
                }
                if (numberDoc == -1) {
                    numberDoc = buf
                    dateDoc = bufDoc
                }
            }
            if (writeJson(scans.toString(), numberDoc, dateDoc, scans.size)) {
                Toast.makeText(applicationContext, "Документы сохранены", Toast.LENGTH_LONG)
                    .show()
                writeJsonFinish()
            } else {
                Toast.makeText(applicationContext, "Ошибка при записи", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


}
