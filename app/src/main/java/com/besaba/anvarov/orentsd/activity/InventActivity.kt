package com.besaba.anvarov.orentsd.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.besaba.anvarov.orentsd.FAPActivityContract
import com.besaba.anvarov.orentsd.R

class InventActivity : AppCompatActivity() {

    private val getNameFAP = registerForActivityResult(FAPActivityContract()) { result ->
        onFAPResult(result)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invent)
        val btnRemains = findViewById<Button>(R.id.btnRemains)
        btnRemains.setOnClickListener { loadRemains() }
        val btnSaveFTP = findViewById<Button>(R.id.btnSaveFTP)
        btnSaveFTP.setOnClickListener { saveInvent() }
        val btnDoc = findViewById<TextView>(R.id.tvDoc)
        btnDoc.setOnClickListener { openDoc() }
        val btnReport = findViewById<TextView>(R.id.tvReport)
        btnReport.setOnClickListener { openReport() }
    }

    private fun loadRemains() {
        getNameFAP.launch(0)
    }

    private fun onFAPResult(nameFAP: String){
        val tvName = findViewById<TextView>(R.id.tvName)
        tvName.text = nameFAP
    }

    private fun saveInvent() {

    }

    private fun openDoc() {
//        val intent = Intent(this@InventActivity, ListFAPActivity::class.java)
//        startActivity(intent)
    }

    private fun openReport() {
//        val intent = Intent(this@InventActivity, ListFAPActivity::class.java)
//        startActivity(intent)
    }
}